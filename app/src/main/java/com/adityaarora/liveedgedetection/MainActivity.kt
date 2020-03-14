package com.adityaarora.liveedgedetection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.adityaarora.liveedgedetection.activity.ScanActivity
import com.adityaarora.liveedgedetection.constants.ScanConstants
import com.adityaarora.liveedgedetection.util.ScanUtils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // let's see if Crashlytics can catch the error now
        Handler().postDelayed({ startScan() }, 2000)
    }

    private fun startScan() {
        val intent = Intent(this, ScanActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.let {
                    val filePath = it.getString(ScanConstants.SCANNED_RESULT)
                    val baseBitmap = ScanUtils.decodeBitmapFromFile(filePath, ScanConstants.IMAGE_NAME)
                    scanned_image.scaleType = ImageView.ScaleType.FIT_CENTER
                    scanned_image.setImageBitmap(baseBitmap)
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                finish()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 101
    }
}