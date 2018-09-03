package com.codertainment.firebasesamples

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.SparseIntArray
import android.view.Menu
import android.view.MenuItem
import android.view.Surface
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.otaliastudios.cameraview.Facing
import com.otaliastudios.cameraview.Flash
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.card_config.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {

  companion object {
    val MODE_TEXT = R.id.button_text
    val MODE_BARCODE = R.id.button_barcode
    val MODE_FACE = R.id.button_face
    val MODE_LABEL = R.id.button_label
    val MODE_LANDMARKS = R.id.button_landmark
  }

  private val ORIENTATIONS = SparseIntArray()
  private lateinit var metadata: FirebaseVisionImageMetadata.Builder
  private var frameCount = 0L
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

    val options = FirebaseVisionFaceDetectorOptions.Builder()
        .setModeType(FirebaseVisionFaceDetectorOptions.FAST_MODE)
        .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
        .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
        .setMinFaceSize(0.15f)
        .setTrackingEnabled(true)
        .build()
    val faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options)

    val labelDetector = FirebaseVision.getInstance().visionLabelDetector

    val landmarkDetector = FirebaseVision.getInstance().visionCloudLandmarkDetector

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
                  data_text.text = fvt.text
                }
                .addOnFailureListener { e ->
                  e.printStackTrace()
                }
          }

          MODE_BARCODE -> {
            barcodeDetector.detectInImage(image)
                .addOnSuccessListener {
                  updateDataSize(it.size)
                  val barcodes = StringBuilder()
                  it.forEachIndexed { index, barcode ->
                    barcodes.append("${index + 1}.\n" +
                        "TYPE: ${barcode.valueType}\n" +
                        "DATA: ${barcode.rawValue}\n" +
                        "FORMAT: ${barcode.format}\n\n")
                  }
                  data_text.text = barcodes
                }
                .addOnFailureListener {
                  it.printStackTrace()
                }
          }

          MODE_FACE -> {
            faceDetector.detectInImage(image)
                .addOnSuccessListener {
                  updateDataSize(it.size)
                  val faces = StringBuilder()
                  it.forEachIndexed { index, face ->
                    faces.append("${index + 1}.\n" +
                        "Bounding Box: ${face.boundingBox}\n" +
                        "Left Eye Open: ${face.leftEyeOpenProbability}\n" +
                        "Right Eye Open: ${face.rightEyeOpenProbability}\n" +
                        "Smiling: ${face.smilingProbability}\n\n")
                  }
                  data_text.text = faces.toString()
                }
                .addOnFailureListener {
                  it.printStackTrace()
                }
          }

          MODE_LABEL -> {
            labelDetector.detectInImage(image)
                .addOnSuccessListener {
                  updateDataSize(it.size)
                  val labels = StringBuilder()
                  it.forEachIndexed { index, label ->
                    labels.append("${index + 1}. ${label.label}: ${label.confidence}\n\n")
                  }
                  data_text.text = labels.toString()
                }
                .addOnFailureListener {
                  it.printStackTrace()
                }
          }

          MODE_LANDMARKS -> {
            landmarkDetector.detectInImage(image)
                .addOnSuccessListener {
                  updateDataSize(it.size)
                  val landmarks = StringBuilder()
                  it.forEachIndexed { index, landmark ->
                    landmarks.append("${index + 1}. ${landmark.landmark}: ${landmark.confidence}\n\n")
                  }
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
      data_text.text = ""
      updateDataSize(0)
      if (mode == MODE_LANDMARKS) {
        toast("Requires billing to be enabled in Google Cloud Console")
      }
    }

    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    data_text.setOnClickListener {
      clipboardManager.primaryClip = ClipData.newPlainText(data_label.text.toString(), data_text.text.toString())
      toast("Copied")
    }

    camera_flash.setOnClickListener {
      camera_flash.switchState(true)
      val drawable = ContextCompat.getDrawable(this, R.drawable.circle) as GradientDrawable
      if (camera_flash.isIconEnabled) {
        drawable.setColor(Color.parseColor("#F44336"))
        camera.flash = Flash.TORCH
      } else {
        drawable.setColor(Color.parseColor("#4CAF50"))
        camera.flash = Flash.OFF
      }
      camera_flash.setBackgroundDrawable(drawable)
    }

    camera_switch.setOnClickListener {
      camera_switch.switchState(true)
      val drawable = ContextCompat.getDrawable(this, R.drawable.circle) as GradientDrawable
      if (camera_switch.isIconEnabled) {
        drawable.setColor(Color.parseColor("#F44336"))
        camera.facing = Facing.FRONT
      } else {
        drawable.setColor(Color.parseColor("#4CAF50"))
        camera.facing = Facing.BACK
      }
      camera_switch.setBackgroundDrawable(drawable)
    }
  }

  private fun updateDataSize(size: Int?) {
    data_text.text = ""
    data_label.text = when (mode) {
      MODE_TEXT -> "Detected Text"

      MODE_BARCODE -> "Detected Barcodes ($size)"

      MODE_FACE -> "Detected Faces ($size)"

      MODE_LABEL -> "Detected Labels ($size)"

      MODE_LANDMARKS -> "Detected Landmarks ($size)"

      else -> "Detected Data"
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

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    if (item?.itemId == R.id.about) {
      startActivity<AboutActivity>()
      return true
    }
    return false
  }
}
