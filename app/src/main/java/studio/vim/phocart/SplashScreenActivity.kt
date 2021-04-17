package studio.vim.phocart

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.billingclient.api.*
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_pro.*
import studio.vim.phocart.utils.PrefManager

class SplashScreenActivity : AppCompatActivity() {

    private var prefManager: PrefManager? = null
    private var billingClient: BillingClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        prefManager = PrefManager(this)
        MobileAds.initialize(this)
        requestReadStoragePermission()
    }


    private fun requestReadStoragePermission() {
        val readStorage = Manifest.permission.READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                        this,
                        readStorage
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(readStorage), 3)
        } else {
            requestWriteStoragePermission()
        }
    }

    private fun requestWriteStoragePermission() {
        val writeStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                        this,
                        writeStorage
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(writeStorage), 3)
        } else {
            initBilling()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            initBilling()
        else {
            super.onBackPressed()
        }
    }

    private fun initBilling() {
        val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
//                    handlePurchase(purchase)
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }

        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(purchasesUpdatedListener)
                .build()

        prefManager?.setPurchaseInfo(false)

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val purchasesResult = billingClient?.queryPurchases(BillingClient.SkuType.SUBS)!! // Or SkuType.SUBS if subscriptions
                    if (purchasesResult.purchasesList != null) {
                        for (purchase in purchasesResult.purchasesList!!) {
                            val statePurchase = purchase.purchaseState
                            if (statePurchase == Purchase.PurchaseState.PURCHASED) {
                                if (purchase.isAcknowledged) {
                                    when (purchase.sku) {
                                        "phocart_yearly_b131" -> prefManager?.setPurchaseInfo(true)
                                        "monthly_phocart_1" -> prefManager?.setPurchaseInfo(true)
                                    }
                                }
                            }
                        }
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(Intent(this@SplashScreenActivity, HomeActivity::class.java))
                        finish()
                    }, 1000)

                }
            }

            override fun onBillingServiceDisconnected() {
                prefManager?.setPurchaseInfo(false)
                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this@SplashScreenActivity, HomeActivity::class.java))
                    finish()
                }, 1000)
            }
        })

    }
}