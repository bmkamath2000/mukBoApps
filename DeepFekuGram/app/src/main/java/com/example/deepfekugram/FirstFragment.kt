package com.example.deepfekugram

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.deepfekugram.R

class FirstFragment : Fragment() {

    private val baseUrl = "https://wa.me/91"
    private val input = StringBuilder()
    private lateinit var urlText: TextView
    private lateinit var keypad: GridLayout
    private val keys = arrayOf("1","2","3","4","5","6","7","8","9","0","DEL","Paste","ENTER")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first, container, false)

        urlText = view.findViewById(R.id.urlText)
        keypad = view.findViewById(R.id.keypad)

        setupKeypad()

        updateUrl()

        return view
    }

    private fun pasteFromClipboard() {
        val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = clipboard.primaryClip

        if (clip != null && clip.itemCount > 0) {
            val pastedText = clip.getItemAt(0).coerceToText(requireContext()).toString()

            // Strip everything except digits
            var digitsOnly = pastedText.filter { it.isDigit() }

            // Handle numbers that include the country code, e.g. "919876543210" or "+919876543210"
            if (digitsOnly.length == 12 && digitsOnly.startsWith("91")) {
                digitsOnly = digitsOnly.substring(2)
            }

            if (digitsOnly.length >= 10) {
                input.clear()
                input.append(digitsOnly.takeLast(10))
            } else if (digitsOnly.isNotEmpty()) {
                input.clear()
                input.append(digitsOnly)
            } else {
                Toast.makeText(requireContext(), "Clipboard has no digits", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Clipboard is empty", Toast.LENGTH_SHORT).show()
        }
    }
    private fun setupKeypad() {
        for (key in keys) {
            val button = Button(requireContext()).apply {
                text = key
                textSize = 20f
                setPadding(0, 20, 0, 20)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(10, 10, 10, 10)
                    gravity = Gravity.CENTER
                }
                setOnClickListener { handleKeyPress(key) }
            }
            keypad.addView(button)
        }
    }

    private fun updateUrl() {
        urlText.text = baseUrl + input.toString()
    }

    private fun handleKeyPress(key: String) {
        when (key) {
            "DEL" -> {
                if (input.isNotEmpty()) input.deleteCharAt(input.length - 1)
            }
            "Paste" ->{
                pasteFromClipboard()
            }
            "ENTER" -> {
                if (input.length == 10) {
                    val fullUrl = baseUrl + input.toString()
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Enter exactly 10 digits", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                if (input.length < 10) input.append(key)
            }
        }
        updateUrl()
    }
}
