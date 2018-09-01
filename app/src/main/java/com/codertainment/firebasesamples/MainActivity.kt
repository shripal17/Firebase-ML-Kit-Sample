package com.codertainment.firebasesamples

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.card_config.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

  companion object {
    val MODE_TEXT = R.id.button_text
    val MODE_BARCODE = R.id.button_barcode
  }

  private val ORIENTATIONS = SparseIntArray()
  lateinit var metadata: FirebaseVisionImageMetadata.Builder
  var frameCount = 0L
  var mode = MODE_TEXT

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ORIENTATIONS.append(Surface.ROTATION_0, 90)
    ORIENTATIONS.append(Surface.ROTATION_90, 0)
    ORIENTATIONS.append(Surface.ROTATION_180, 270)
    ORIENTATIONS.append(Surface.ROTATION_270, 180)

    val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
    val barcodeDetector = FirebaseVision.getInstance().visionBarcodeDetector

    metadata = FirebaseVisionImageMetadata.Builder()
        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)

    camera.addFrameProcessor {
      if (frameCount++ % 24 != 0L) {
        return@addFrameProcessor
      }
      try {
        val size = camera.previewSize
        if (size != null) {
          metadata
              .setHeight(size.height)
              .setWidth(size.width)
        }

        var rotationCompensation = ORIENTATIONS.get(this@MainActivity.windowManager.defaultDisplay.rotation)
        rotationCompensation = (rotationCompensation + it.rotation + 270) % 360
        val result: Int
        when (rotationCompensation) {
          0 -> result = FirebaseVisionImageMetadata.ROTATION_0
          90 -> result = FirebaseVisionImageMetadata.ROTATION_90
          180 -> result = FirebaseVisionImageMetadata.ROTATION_180
          270 -> result = FirebaseVisionImageMetadata.ROTATION_270
          else -> {
            result = FirebaseVisionImageMetadata.ROTATION_0
            Log.e("rot", "Bad rotation value: $rotationCompensation")
          }
        }

        metadata.setRotation(result)

        val image = FirebaseVisionImage.fromByteArray(it.data, metadata.build())

        when (mode) {
          MODE_TEXT -> {
            textRecognizer.processImage(image)
                .addOnSuccessListener { fvt ->
                  Log.d("detected", fvt.text)
                  data_text.text = fvt.text
                }
                .addOnFailureListener { e ->
                  e.printStackTrace()
                }
          }

          MODE_BARCODE -> {
            barcodeDetector.detectInImage(image)
                .addOnSuccessListener {
                  val barcodes = StringBuilder()
                  for (barcode in it) {
                    barcodes.append("TYPE: ${barcode.valueType}\nDATA:${barcode.rawValue}\nFORMAT:${barcode.format}\n\n")
                  }
                  data_text.text = barcodes
                }
                .addOnFailureListener {
                  it.printStackTrace()
                }
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    camera_play_pause.change(false)

    camera_play_pause.setOnClickListener {
      camera_play_pause.toggle()
      if (camera_play_pause.isPlay) {
        camera.stop()
      } else {
        camera.start()
      }
    }

    setSupportActionBar(toolbar)

    config_group.setOnCheckedChangeListener { _, i ->
      mode = i
      when (mode) {
        MODE_TEXT -> {
          data_label.text = "Detected Text"
        }

        MODE_BARCODE -> {
          data_label.text = "Detected Barcodes"
        }
      }
    }

    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    data_text.setOnClickListener {
      clipboardManager.primaryClip = ClipData.newPlainText(data_label.text.toString(), data_text.text.toString())
    }
  }

  override fun onResume() {
    super.onResume()
    if (!camera_play_pause.isPlay) {
      camera.start()
    } else {
      camera.stop()
    }
  }

  override fun onPause() {
    super.onPause()
    camera.stop()
  }

  override fun onDestroy() {
    super.onDestroy()
    camera.destroy()
  }
}
