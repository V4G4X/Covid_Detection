package com.example.covid_detection

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.covid_detection.ml.Model
import com.google.android.material.snackbar.Snackbar
import org.tensorflow.lite.support.image.TensorImage
import kotlin.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var bitmap: Bitmap
    private lateinit var imgView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgView = findViewById(R.id.imageView)
        val tv:TextView = findViewById(R.id.textView)

        val select: Button = findViewById(R.id.button_select)
        select.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        val predict:Button = findViewById(R.id.button_predict)
        predict.setOnClickListener{
            try {
                val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
                val model = Model.newInstance(this)

                // Creates inputs for reference.
                val image = TensorImage.fromBitmap(resized)
                // Runs model inference and gets result.
                val outputs = model.process(image)
                val probability = outputs.probabilityAsCategoryList
                var max = Float.MIN_VALUE
                var label = probability[0].label
                probability.forEach {
                    if (it.score > max){
                        max = it.score
                        label = it.label
                    }
                }
                if(label.equals("Covid")){
                    label = "Covid Positive"
                    tv.setTextColor(Color.RED)
                }
                else {
                    label = "Covid Negative"
                    tv.setTextColor(Color.GREEN)
                }
                tv.text = label
                // Releases model resources if no longer used.
                model.close()
            }
            catch (e: UninitializedPropertyAccessException){
                val contextView = findViewById<View>(R.id.layout_base)
                Snackbar.make(contextView, "Please select an Image first", Snackbar.LENGTH_SHORT).setAction("Dismiss"){}.show()
            }
            catch (e: Exception){
                val contextView = findViewById<View>(R.id.layout_base)
                Snackbar.make(contextView, "Some Error Occurred", Snackbar.LENGTH_SHORT).setAction("Dismiss"){}.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        imgView.setImageURI(data?.data)
        val uri: Uri? = data?.data
        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
    }

}