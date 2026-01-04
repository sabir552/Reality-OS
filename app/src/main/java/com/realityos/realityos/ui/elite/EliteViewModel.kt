package com.realityos.realityos.ui.elite

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import com.realityos.realityos.data.local.entity.HistoryEventEntity
import com.realityos.realityos.data.repository.RealityOSRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val ELITE_SKU = "reality_os_elite_commitment" // Your Product ID from Play Console

class EliteViewModel(
    application: Application,
    private val repository: RealityOSRepository,
    private val savedStateHandle: SavedStateHandle // Used to survive process death
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EliteUiState())
    val uiState: StateFlow<EliteUiState> = _uiState.asStateFlow()

    private lateinit var billingClient: BillingClient
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                viewModelScope.launch {
                    handlePurchase(purchase)
                }
            }
        } else {
             _uiState.update { it.copy(isLoading = false, message = "Purchase failed or cancelled.") }
        }
    }

    init {
        setupBillingClient()
        checkQualification()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(getApplication())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("EliteViewModel", "Billing client setup finished.")
                    querySkuDetails()
                    queryPurchases() // Check for existing purchases
                } else {
                     _uiState.update { it.copy(message = "Error connecting to billing service.") }
                }
            }
            override fun onBillingServiceDisconnected() {
                // Retry connection
                Log.d("EliteViewModel", "Billing service disconnected.")
            }
        })
    }

    private fun querySkuDetails() {
         val skuList = listOf(ELITE_SKU)
         val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                val eliteSku = skuDetailsList.firstOrNull { it.sku == ELITE_SKU }
                _uiState.update { it.copy(skuDetails = eliteSku) }
            }
        }
    }
    
    // Check for existing elite purchases on startup
    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { purchase ->
                    if (purchase.skus.contains(ELITE_SKU) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        viewModelScope.launch { handlePurchase(purchase) }
                    }
                }
            }
        }
    }

    private fun checkQualification() {
        viewModelScope.launch {
            repository.getUser().first()?.let { user ->
                // Example logic: 30-day streak, 10000 XP, no breaks
                val qualified = user.streak >= 30 && user.xp >= 10000 && !user.hasBrokenCommitment && user.commitmentLevel != "ELITE"
                _uiState.update { it.copy(isQualified = qualified) }
            }
        }
    }

    fun launchBillingFlow(activity: Activity) {
        val skuDetails = uiState.value.skuDetails ?: return
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
        if (responseCode != BillingClient.BillingResponseCode.OK) {
             _uiState.update { it.copy(message = "Failed to launch purchase flow.") }
        } else {
             _uiState.update { it.copy(isLoading = true) }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                         // Acknowledged, now grant entitlement
                         grantEliteEntitlement()
                    }
                }
            } else {
                // Already acknowledged, just ensure entitlement is granted
                grantEliteEntitlement()
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
             _uiState.update { it.copy(isLoading = false, message = "Purchase is pending.") }
        }
    }
    
    private fun grantEliteEntitlement() {
        viewModelScope.launch {
             val currentUser = repository.getUser().first()
             if (currentUser != null && currentUser.commitmentLevel != "ELITE") {
                  repository.updateUser(currentUser.copy(commitmentLevel = "ELITE"))
                  repository.logHistoryEvent(
                      HistoryEventEntity(
                          timestamp = System.currentTimeMillis(),
                          eventDescription = "Commitment upgraded to ELITE."
                      )
                  )
                  _uiState.update { it.copy(isLoading = false, message = "Elite Commitment Unlocked!", isElite = true) }
             } else {
                 // User is already elite or doesn't exist, handle silently
                 _uiState.update { it.copy(isLoading = false, isElite = true) }
             }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
    }
}

data class EliteUiState(
    val isQualified: Boolean = false,
    val isElite: Boolean = false,
    val isLoading: Boolean = false,
    val skuDetails: SkuDetails? = null,
    val message: String? = null
)
