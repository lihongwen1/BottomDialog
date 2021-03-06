package cn.vove7.bottomdialog

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.SparseArray
import androidx.appcompat.app.AppCompatActivity
import cn.vove7.bottomdialog.builder.BottomDialogBuilder

/**
 * # BottomDialogActivity
 *
 * @author Vove
 * 2019/6/29
 */

class BottomDialogActivity : AppCompatActivity() {

    private lateinit var dialog: BottomDialog

    companion object {
        /**
         * 构造器
         * @param context Context
         * @param action [@kotlin.ExtensionFunctionType] Function1<BottomDialogBuilder, Unit>
         */
        fun builder(context: Context, action: BottomDialogBuilder.() -> Unit) {
            val b = BottomDialogBuilder(context).apply(action)
            start(context, b)
        }

        @Synchronized
        private fun start(context: Context, dialog: BottomDialogBuilder) {
            val i = Intent(context, BottomDialogActivity::class.java)
            i.putExtra("tag", dialog.hashCode())
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            initCache(dialog)
            context.startActivity(i)
        }

        @Synchronized
        private fun initCache(dialog: BottomDialogBuilder) {
            if (dialogArray == null) {
                dialogArray = SparseArray()
            }
            dialogArray?.put(dialog.hashCode(), dialog)
        }

        @Synchronized
        private fun clearCache(tag: Int) {
            dialogArray?.remove(tag)
            if (dialogArray?.size() == 0) {
                dialogArray = null
            }
        }

        private var dialogArray: SparseArray<BottomDialogBuilder>? = null
    }

    private var dialogTag: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val i = intent
        if (i?.hasExtra("tag") != true) {
            finish()
            return
        }
        dialogTag = i.getIntExtra("tag", 0)
        try {
            dialog = BottomDialog(dialogArray?.get(dialogTag)!!.also {
                it.context = this@BottomDialogActivity
            })
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask()
            } else {
                finish()
            }
        }
        dialog.setOnDismissListener {
            finish()
        }
        //延迟100ms保证正确识别到导航栏
        Handler().postDelayed({
            dialog.show()
        }, 100)
    }

    override fun onDestroy() {
        super.onDestroy()
        clearCache(dialogTag)
    }
}