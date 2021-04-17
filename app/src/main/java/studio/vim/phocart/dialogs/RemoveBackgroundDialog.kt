package studio.vim.phocart.dialogs

import android.app.Activity
import android.app.Dialog
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.widget.AppCompatButton
import studio.vim.phocart.R


/**
 * Created by Gilang Arinata on 28/03/21.
 * https://github.com/gilangarinata/
 */
class RemoveBackgroundDialog {
    fun showDialog(activity: Activity, msg: String, listener: BackgroundDialogListener) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.remove_background_dialog)

        val noBackgoundBtn = dialog.findViewById(R.id.btnRemoveBackground) as AppCompatButton
        val useBackgoundBtn = dialog.findViewById(R.id.btnUseBackground) as AppCompatButton

        noBackgoundBtn.setOnClickListener {
            dialog.dismiss()
            listener.onNoBackgroundSelected()
        }
        useBackgoundBtn.setOnClickListener {
            dialog.dismiss()
            listener.onUseBackgroundSelected()
        }
        dialog.show()
        val window: Window = dialog.window!!
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    interface BackgroundDialogListener {
        fun onNoBackgroundSelected()
        fun onUseBackgroundSelected()
    }
}