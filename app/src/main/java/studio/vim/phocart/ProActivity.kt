package studio.vim.phocart

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import kotlinx.android.synthetic.main.activity_pro.*


class ProActivity : AppCompatActivity() {

    private var selectedSku: SkuDetails? = null
    private var billingClient: BillingClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pro)
        ivClose.setOnClickListener { this.finish() }
        setupBillings()
    }

    private fun handlePurchase(purchase: Purchase?) {
        if (purchase?.purchaseState == Purchase.PurchaseState.PURCHASED) {
            Toast.makeText(this, "purchased", Toast.LENGTH_LONG).show()
        }

        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.

        val consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase!!.purchaseToken)
                        .build()

        billingClient?.consumeAsync(consumeParams) { billingResult, outToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Handle the success of the consume operation.
                Toast.makeText(this, "consumed", Toast.LENGTH_LONG).show()
            }
        }

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams.build()) {


                }
            }
        }
    }

    private fun setupBillings() {
        val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val purchasesResult = billingClient?.queryPurchases(BillingClient.SkuType.SUBS)!! // Or SkuType.SUBS if subscriptions
                    if (purchasesResult.purchasesList != null) {
                        for (purchase in purchasesResult.purchasesList!!) {
                            if (purchase.isAcknowledged) {
                                when (purchase.sku) {
                                    "phocart_yearly_b131" -> Toast.makeText(this@ProActivity, "yearly subscribe", Toast.LENGTH_LONG).show()
                                    "monthly_phocart_1" -> Toast.makeText(this@ProActivity, "monthly subscribe", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }

                    val skuList = ArrayList<String>()
                    skuList.add("phocart_yearly_b131")
                    skuList.add("monthly_phocart_1")
                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)

                    billingClient?.querySkuDetailsAsync(params.build()
                    ) { billingResult, skuDetailsList ->
                        if (skuDetailsList != null) {
                            if (skuDetailsList.size > 1) {
                                radioPrice1.text = skuDetailsList?.get(0)?.price + "/month"
                                radioPrice2.text = skuDetailsList?.get(1)?.price + "/year"
                                progressBar.visibility = View.GONE
                                lytPrice.visibility = View.VISIBLE
                            } else {
                                radioPrice1.text = skuDetailsList?.get(0)?.price + "/month"
                                selectedSku = skuDetailsList?.get(0)
                                progressBar.visibility = View.GONE
                                lytPrice.visibility = View.VISIBLE
                            }

                            selectedSku = skuDetailsList?.get(0)

                            radio.setOnCheckedChangeListener { radioGroup, i ->
                                selectedSku = if (radioGroup.tag == "price_monthly") {
                                    skuDetailsList?.get(0)
                                } else {
                                    skuDetailsList?.get(1)
                                }
                            }
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })

        btnContinue.setOnClickListener {
            val billingFlowParams = selectedSku?.let { it1 ->
                BillingFlowParams.newBuilder()
                        .setSkuDetails(it1)
                        .build()
            }
            val responseCode = billingFlowParams?.let { it1 -> billingClient?.launchBillingFlow(this, it1)?.responseCode }

        }


    }


}