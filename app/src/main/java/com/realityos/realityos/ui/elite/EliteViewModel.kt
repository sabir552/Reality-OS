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

private const val ELITE_PRODUCT_ID = "reality_os_elite_commitment" // Your Product ID from Play Console

class EliteViewModel(
    application: Application,
    private val repository: RealityOSRepository,
    private val savedStateHandle: SavedStateHandle
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
                if (billingResult.responseCode == BillingClient.BillingResponseC.OK) {
                    Log.d("EliteViewModel", "Billing client setup finished.")
                    queryProductDetails()
                    queryPurchases()
                } else {
                     _uiState.update { it.copy(message = "Error connecting to billing service.") }
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.d("EliteViewModel", "Billing service disconnected.")
            }
        })
    }

    private fun queryProductDetails() {
         val productList = listOf(
             QueryProductDetailsParams.Product.newBuilder()
                .setProductId(ELITE_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
         )
         val params = QueryProductDetailsParams.newBuilder().setProductList(productList)

        billingClient.queryProductDetailsAsync(params.build()) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val eliteProductDetails = productDetailsList.first()
                _uiState.update { it.copy(productDetails = eliteProductDetails) }
            }
        }
    }

    private fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
        billingClient.queryPurchasesAsync(params.build()) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { purchase ->
                    if (purchase.products.contains(ELITE_PRODUCT_ID) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        viewModelScope.launch { handlePurchase(purchase) }
                    }
                }
            }
        }
    }

    private fun checkQualification() {
        viewModelScope.launch {
            repository.getUser().first()?.let { user ->
                val qualified = user.streak >= 30 && user.xp >= 10000 && !user.hasBrokenCommitment && user.commitmentLevel != "ELITE"
                _uiState.update { it.copy(isQualified = qualified) }
            }
        }
    }

    fun launchBillingFlow(activity: Activity) {
        val productDetails = uiState.value.productDetails ?: return
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).responseCode
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
                         grantEliteEntitlement()
                    }
                }
            } else {
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
    val productDetails: ProductDetails? = null, // Changed from SkuDetails
    val message: String? = null
)
