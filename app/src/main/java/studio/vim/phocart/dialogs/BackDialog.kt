package studio.vim.phocart.dialogs

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import studio.vim.phocart.R


/**
 * Created by Gilang Arinata on 28/03/21.
 * https://github.com/gilangarinata/
 */
class BackDialog {
    fun showDialog(activity: Activity, msg: String, listener: BackDialogListener) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.exit_bottom_sheet)

        val dialogButton = dialog.findViewById(R.id.btnCancel) as AppCompatButton
        val exitDialog = dialog.findViewById(R.id.btnOk) as TextView
        val desc = dialog.findViewById(R.id.tvDesc) as TextView
        desc.text = msg
        dialogButton.setOnClickListener {
            dialog.dismiss()
        }
        exitDialog.setOnClickListener {
            dialog.dismiss()
            listener.onExitClicked()
        }
        dialog.show()
    }

    interface BackDialogListener {
        fun onExitClicked()
    }
}