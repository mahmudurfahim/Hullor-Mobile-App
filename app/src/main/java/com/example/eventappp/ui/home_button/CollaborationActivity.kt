package com.example.eventappp.ui.home_button

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.eventappp.MainActivity
import com.example.eventappp.R
import com.example.eventappp.ui.auth.RegisterActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CollaborationActivity : AppCompatActivity() {

    private val scriptUrl =
        "https://script.google.com/macros/s/AKfycbzoYdZnhVG_fZeF0ZFPL-l6rUuqfIeQpcYalDHlWIC05q3AyrjKtmibixj170NlKAY7dg/exec"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collaboration)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            // Dark icons? (if background is light)
            isAppearanceLightStatusBars = true
        }

        window.statusBarColor = Color.TRANSPARENT

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etEventDate = findViewById<EditText>(R.id.etEventDate)
        val etAddress = findViewById<EditText>(R.id.etEventAddress)
        val spinnerEventType = findViewById<Spinner>(R.id.spinnerEventType)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        val formLayout = findViewById<LinearLayout>(R.id.formLayout)
        val collabNote = findViewById<TextView>(R.id.collabNote)
        val collaborateBtn = findViewById<Button>(R.id.collaborateWithHullorBtn)

        setupSpinner(spinnerEventType)
        setupDatePicker(etEventDate)


        collaborateBtn.setOnClickListener {
            // Hide collab note and button
            collabNote.visibility = TextView.GONE
            collaborateBtn.visibility = Button.GONE

            // Show form
            formLayout.visibility = LinearLayout.VISIBLE
        }


        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }


        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val eventDate = etEventDate.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val eventType = spinnerEventType.selectedItem.toString()
            val message = etMessage.text.toString().trim()



            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() ||
                eventDate.isEmpty() || address.isEmpty() || message.isEmpty()
            ) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // Show thank-you dialog immediately
            showThankYouDialog()

            // Send data in background
            sendToGoogleSheet(name, phone, email, eventDate, address, eventType, message)
        }


    }



    private fun setupSpinner(spinner: Spinner) {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.event_types,
            android.R.layout.simple_spinner_dropdown_item
        )
        spinner.adapter = adapter
    }

    private fun setupDatePicker(editText: EditText) {
        editText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dp = DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    val format = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                    editText.setText(format.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dp.datePicker.minDate = System.currentTimeMillis() // future dates only
            dp.show()
        }
    }

    private fun sendToGoogleSheet(
        name: String,
        phone: String,
        email: String,
        eventDate: String,
        address: String,
        eventType: String,
        message: String
    ) {
        val client = OkHttpClient()
        val json = JSONObject()
        json.put("name", name)
        json.put("phone", phone)
        json.put("email", email)
        json.put("eventDate", eventDate)
        json.put("address", address)
        json.put("eventType", eventType)
        json.put("message", message)

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            json.toString()
        )

        val request = Request.Builder()
            .url(scriptUrl)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@CollaborationActivity,
                        "Failed! Check Internet.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Data sent successfully, nothing else needed since dialog is already shown
            }
        })
    }

    private fun showThankYouDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(
            "Thank you for your interest in Hullor.\n" +
                    "A member from Hullor team will contact you soon."
        )
        dialogBuilder.setCancelable(false)
        dialogBuilder.setPositiveButton("Go Back to Home") { dialog, _ ->
            dialog.dismiss()
            finish() // Close activity
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        // Center the button text and make full width
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            val params = layoutParams as LinearLayout.LayoutParams
            params.width = LinearLayout.LayoutParams.MATCH_PARENT
            layoutParams = params
            textAlignment = Button.TEXT_ALIGNMENT_CENTER
        }
    }
    override fun onBackPressed() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }
}
