package br.com.fiap.esg

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.com.fiap.esg.ui.theme.ESGTheme
import br.com.fiap.esg.RegisterActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    // private var isPasswordVisible = false
    private lateinit var emailLogin: EditText
    private lateinit var passwordLogin: EditText
    private lateinit var buttonLogin: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var linkRegister: TextView
    //private lateinit var presentationText: TextView
    private lateinit var buttonDecrease: Button
    private lateinit var buttonIncrease: Button
    private var textSize = 20f
    private val textSizeStep = 2f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*val password = findViewById<EditText>(R.id.password)
        val showPassword = findViewById<ImageView>(R.id.showPassword)

        showPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                password.transformationMethod = null
                showPassword.setImageResource(R.mipmap.ic_close_eye)
            } else {
                password.transformationMethod = PasswordTransformationMethod.getInstance()
                showPassword.setImageResource(R.drawable.ic_visibility_off)
            }
            password.setSelection(password.text.length)
        }*/
        initializeViews()
        setupFirebase()
        setupLoginButton()
        setupRegisterLink()
        setupTextSizeControls()
    }

    private fun initializeViews() {
        auth = Firebase.auth
        emailLogin = findViewById(R.id.emailLogin)
        passwordLogin = findViewById(R.id.passwordLogin)
        buttonLogin = findViewById(R.id.buttonLogin)
        linkRegister = findViewById(R.id.linkRegister)
        //presentationText = findViewById(R.id.presentationText)
        buttonIncrease = findViewById(R.id.buttonIncreaseTextSize)
        buttonDecrease = findViewById(R.id.buttonDecreaseTextSize)
    }

    private fun setupFirebase() {
        auth = Firebase.auth
    }

    private fun setupLoginButton() {
        buttonLogin.setOnClickListener {
            val email = emailLogin.text.toString()
            val password = passwordLogin.text.toString()
            loginUser(email, password)
        }
    }

    private fun setupRegisterLink() {
        linkRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupTextSizeControls() {
        buttonIncrease.setOnClickListener {
            textSize += textSizeStep
            adjustTextSize()
        }

        buttonDecrease.setOnClickListener {
            textSize -= textSizeStep
            adjustTextSize()
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToDashboard()
                } else {
                    showLoginError()
                }
            }
    }

    private fun adjustTextSize() {
        if (textSize in 20f..28f) {
            //presentationText.textSize = textSize
            emailLogin.textSize = textSize
            passwordLogin.textSize = textSize
            buttonLogin.textSize = textSize
            linkRegister.textSize = textSize
        } else {
            showTextSizeLimitError()
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    private fun showLoginError() {
        Toast.makeText(baseContext, "E-mail ou senha incorreto!", Toast.LENGTH_SHORT).show()
    }

    private fun showTextSizeLimitError() {
        Toast.makeText(baseContext, "Opa! Este é o limite máximo para aumentar/diminuir o tamanho da letra para visualização.", Toast.LENGTH_SHORT).show()
    }
}