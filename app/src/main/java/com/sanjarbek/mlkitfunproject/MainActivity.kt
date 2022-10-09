package com.sanjarbek.mlkitfunproject
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.widget.Toast
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.android.material.dialog.MaterialDialogs
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.common.Barcode.TYPE_PRODUCT
import com.google.mlkit.vision.barcode.common.Barcode.TYPE_UNKNOWN
import com.google.mlkit.vision.common.InputImage
import com.sanjarbek.mlkitfunproject.databinding.ActivityMainBinding
import java.lang.reflect.Array.newInstance
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity: AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TAG", "kirfi: ")

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        Log.d("TAG", "onCreate: ")


        // Request camera permissions
        if (allPermissionsGranted()) {
            Log.d(TAG, "onCreate: ")

            startCamera()
        } else {
            Log.d(TAG, "onCreate: ")
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the listeners for take photo and video capture buttons

        cameraExecutor = Executors.newSingleThreadExecutor()

        val historyBtn = viewBinding.historyBtn
        historyBtn.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }




    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()


            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, YourImageAnalyzer())

                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private inner class YourImageAnalyzer : ImageAnalysis.Analyzer {
        @SuppressLint("UnsafeOptInUsageError", "RestrictedApi")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                val scanner = BarcodeScanning.getClient()

                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes){

                            val rawValue = barcode.rawValue

                            // See API reference for complete list of supported types
                            when (barcode.valueType) {
                                Barcode.TYPE_WIFI -> {
                                    val ssid = barcode.wifi!!.ssid
                                    val password = barcode.wifi!!.password
                                    val type = barcode.wifi!!.encryptionType
                                    Toast.makeText(this@MainActivity, "SSID: $ssid, Password: $password, Type: $type", Toast.LENGTH_LONG).show()
                                }
                                Barcode.TYPE_URL -> {
                                    val title = barcode.url!!.title
                                    val url = barcode.url!!.url
                                    Toast.makeText(this@MainActivity, "$title, aresss $url", Toast.LENGTH_SHORT).show()
                                }
                                TYPE_PRODUCT ->{
                                    val price = barcode!!.rawValue
                                    Toast.makeText(this@MainActivity, "kiddi $price", Toast.LENGTH_SHORT).show()
                                }
                                TYPE_UNKNOWN ->{
                                    val smth = barcode.rawValue
                                    Toast.makeText(this@MainActivity, "$smth", Toast.LENGTH_LONG).show()
                                }
                            }
                            db = Firebase.firestore


                            val dialog = MaterialDialog(this@MainActivity)
                                .noAutoDismiss()
                                .customView(R.layout.layout_dialog_add)


                            dialog.findViewById<Button>(R.id.btn_add).setOnClickListener {

                                val name = dialog.findViewById<EditText>(R.id.pr_name_edt).text.toString()
                                val simpleDate = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.US)
                                val currentDate = simpleDate.format(Date())

                                val value = rawValue

                                val product = hashMapOf(
                                    "product name" to name,
                                    "product id" to value,
                                    "product date" to currentDate
                                )

                                Log.d("kir", "analyze: $name $currentDate")

                                db.collection("products")
                                    .add(product)
                                    .addOnSuccessListener {
                                        Log.d("kir", "buldi:Added $product for $currentDate")
                                        Toast.makeText(this@MainActivity, "Added $product for $currentDate", Toast.LENGTH_SHORT)
                                            .show()

                                    }
                                    .addOnFailureListener{
                                        Log.d("kir", "bomadi:Added $product for $currentDate")

                                        Toast.makeText(this@MainActivity, "Failed to add $product for $currentDate", Toast.LENGTH_SHORT)
                                            .show()
                                    }

                                dialog.dismiss()

                            }
                            dialog.show()

                        }
                    }
                    .addOnFailureListener {
                        Log.d("failSc", "analyze: failure")
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
                imageProxy.close()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        Log.d(TAG, "onRequestPermissionsResult: ")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}

