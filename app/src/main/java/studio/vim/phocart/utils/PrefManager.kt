package studio.vim.phocart.utils

import android.app.Activity
import android.content.Context


/**
 * Created by Gilang Arinata on 17/04/21.
 * https://github.com/gilangarinata/
 */
class PrefManager(private val activity: Activity) {
    private val APP_KEY: String = "phocart"
    private val KEY_PREMIUM: String = "key_premium"

    fun isPurchaseUser(): Boolean {
        val sharedPref = activity.getSharedPreferences(APP_KEY, Context.MODE_PRIVATE)
                ?: return false
        return sharedPref.getBoolean(KEY_PREMIUM, false)
    }

    fun setPurchaseInfo(isPremium: Boolean) {
        val sharedPref = activity.getSharedPreferences(APP_KEY, Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putBoolean(KEY_PREMIUM, isPremium)
            apply()
        }
    }


}