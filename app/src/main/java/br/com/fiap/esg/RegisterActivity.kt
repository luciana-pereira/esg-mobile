package br.com.fiap.esg

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.*
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import br.com.fiap.esg.HomeActivity
import br.com.fiap.esg.MainActivity
import br.com.fiap.esg.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.util.UUID

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var spinnerSelectProfession: Spinner
    private lateinit var emailRegister: EditText
    private lateinit var passwordRegister: EditText
    private lateinit var buttonRegister: Button
    private lateinit var linkLogin: TextView
    private lateinit var radioButtonCollaborator: RadioButton
    private lateinit var radioButtonAdministrator: RadioButton
    private lateinit var spinnerProfession: RelativeLayout
    private lateinit var textProfession: TextView
    private lateinit var userName: EditText
    private lateinit var surname: EditText
    private lateinit var radioButtonFemale: RadioButton
    private lateinit var radioButtonMale: RadioButton
    private lateinit var checkBoxPrivacyPolicy: CheckBox
    private lateinit var dateOfBirth: EditText
    private lateinit var storageReference: StorageReference
    private lateinit var imageView: ImageView
    //private lateinit var imageUrl: String
    private val PICK_IMAGE_REQUEST = 1
    private var filePath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        storageReference = FirebaseStorage.getInstance().reference
        imageView = findViewById(R.id.imageView)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val buttonChooseImage: Button = findViewById(R.id.buttonChooseImage)
        buttonChooseImage.setOnClickListener {
            showFileChooser()
        }

        initializeComponents()
        setupSpinner()
        setupTextWatchers()
        setupListeners()
        disableButton()
        validateFields()
    }

    private fun showFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Escolha uma imagem"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            var filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage() {
        if (filePath != null) {
            val ref: StorageReference = storageReference.child("images/" + UUID.randomUUID().toString())
            val stream = ByteArrayOutputStream()
            val bitmap = (imageView.drawable).toBitmap()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val byteArray = stream.toByteArray()

            ref.putBytes(byteArray)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    ref.downloadUrl
                }
                .addOnCompleteListener { downloadTask ->
                    if (downloadTask.isSuccessful) {
                        val imageUrl = downloadTask.result.toString()
                        //registerUser(emailRegister.text.toString(), passwordRegister.text.toString(), imageUrl)
                    } else {
                        Toast.makeText(
                            this,
                            "Erro ao obter URL de download da imagem.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun initializeComponents() {
        spinnerSelectProfession = findViewById(R.id.spinnerSelectProfession)
        emailRegister = findViewById(R.id.emailRegister)
        passwordRegister = findViewById(R.id.passwordRegister)
        buttonRegister = findViewById(R.id.buttonRegister)
        spinnerProfession = findViewById(R.id.spinnerProfession)
        textProfession = findViewById(R.id.textProfession)
        radioButtonCollaborator = findViewById(R.id.radioButtonCollaborator)
        radioButtonAdministrator = findViewById(R.id.radioButtonAdministrator)
        linkLogin = findViewById(R.id.linkLogin)
        userName = findViewById(R.id.editTextName)
        surname = findViewById(R.id.editTextSobrenome)
        radioButtonFemale = findViewById(R.id.radioButtonFemale)
        radioButtonMale = findViewById(R.id.radioButtonMale)
        checkBoxPrivacyPolicy = findViewById(R.id.checkBoxPrivacyPolicy)
        dateOfBirth = findViewById(R.id.editTextDate)
    }

    private fun setupSpinner() {
        val specialties = listOf(
            "",
            "Administrador",
            "RH",
            "CEO",
            "Outros"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSelectProfession.adapter = adapter

        spinnerSelectProfession.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                validateFields()
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                Toast.makeText(
                    baseContext, "Informe sua profissão, para concluir o cadastro.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupTextWatchers() {
        emailRegister.addTextChangedListener(watcher)
        passwordRegister.addTextChangedListener(watcher)
    }

    private fun setupListeners() {
        radioButtonCollaborator.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                hideProfessionalFields()
            }
            validateFields()
        }

        radioButtonAdministrator.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showProfessionalFields()
            }
            validateFields()
        }

        buttonRegister.setOnClickListener {
            uploadImage()
            val email = emailRegister.text.toString()
            val password = passwordRegister.text.toString()

            registerUser(email, password)
        }

        linkLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun disableButton() {
        buttonRegister.isEnabled = false
    }

    private fun showProfessionalFields() {
        spinnerProfession.visibility = View.VISIBLE
        textProfession.visibility = View.VISIBLE
    }

    private fun hideProfessionalFields() {
        spinnerProfession.visibility = View.GONE
        textProfession.visibility = View.GONE
    }

    private fun validateFields() {
        val isCollaboratorSelected = radioButtonCollaborator.isChecked
        val isProfessionalSelected = radioButtonAdministrator.isChecked
        val isEmailValid = emailRegister.text.isNotEmpty()
        val isPasswordValid = passwordRegister.text.isNotEmpty()
        val isPasswordValidContent = passwordRegister.text.toString()
        val isSpinnerItemSelected =
            if (isProfessionalSelected) spinnerSelectProfession.selectedItemPosition != AdapterView.INVALID_POSITION else true
        val isUserName = userName.text.isNotEmpty()
        val isSurname = surname.text.isNotEmpty()
        val isFemale = radioButtonFemale.isChecked
        val isMale = radioButtonMale.isChecked
        val isDateOfBirthDate = dateOfBirth.text.isNotEmpty()
        val isPrivacyPolicy = checkBoxPrivacyPolicy.isChecked

        if (!isProfessionalSelected) {
            buttonRegister.isEnabled = isUserName &&
                    isSurname &&
                    isEmailValid &&
                    (isPasswordValid && isPasswordValidContent.length >= 6) &&
                    (isFemale || isMale) &&
                    isDateOfBirthDate &&
                    isPrivacyPolicy
        } else {
            buttonRegister.isEnabled = isUserName &&
                    isSurname &&
                    isEmailValid &&
                    (isPasswordValid && isPasswordValidContent.length >= 6) &&
                    isSpinnerItemSelected &&
                    (isFemale || isMale) &&
                    isDateOfBirthDate &&
                    isPrivacyPolicy
        }
    }

    private val watcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun afterTextChanged(editable: Editable) {
            validateFields()
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val user = if (radioButtonCollaborator.isChecked) {
                            hashMapOf(
                                "type" to "collaborator",
                                "imageUrl" to "",
                                "name" to userName.text.toString(),
                                "surname" to surname.text.toString(),
                                "email" to emailRegister.text.toString(),
                                "password" to passwordRegister.text.toString(),
                                "gender" to if (radioButtonFemale.isChecked) "Feminino" else "Masculino",
                                "dateOfBirth" to dateOfBirth.text.toString()
                            )
                        } else {
                            hashMapOf(
                                "type" to "administrator",
                                "imageUrl" to "",
                                "name" to userName.text.toString(),
                                "surname" to surname.text.toString(),
                                "email" to emailRegister.text.toString(),
                                "password" to passwordRegister.text.toString(),
                                "Profissão" to textProfession.text.toString(),
                                "gender" to if (radioButtonFemale.isChecked) "Feminino" else "Masculino",
                                "dateOfBirth" to dateOfBirth.text.toString()
                            )
                        }

                        //val collection = if (radioButtonCollaborator.isChecked) "collaborator" else "administrator"

                        firestore.collection("users").document(userId)
                            .set(user)
                            .addOnCompleteListener { registrationTask ->
                                if (registrationTask.isSuccessful) {
                                    Log.d(TAG, "DocumentSnapshot added")
                                    Toast.makeText(
                                        this,
                                        "Cadastro realizado com sucesso!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this, HomeActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    Log.w(TAG, "Error adding document", registrationTask.exception)
                                    Toast.makeText(
                                        this,
                                        "Erro ao realizar cadastro: ${registrationTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Log.e("SignUpActivity", "Erro ao criar usuário: ${task.exception?.message}")
                    }
                } else {
                    Toast.makeText(
                        baseContext, "Erro ao criar usuário: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}