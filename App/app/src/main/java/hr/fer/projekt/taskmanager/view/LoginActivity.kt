package hr.fer.projekt.taskmanager.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import hr.fer.projekt.taskmanager.R
import hr.fer.projekt.taskmanager.model.DatabaseHelper
import hr.fer.projekt.taskmanager.utility.Constants
import hr.fer.projekt.taskmanager.view.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_login.*
import java.io.File


class LoginActivity : AppCompatActivity() {

    private lateinit var pref: SharedPreferences
    private var googleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null
    // private var googleDriveService:Drive?=null
    // private var mDriveServiceHelper:DriveServiceHelper?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("159322618714-h9mtbg3q95tl7q5nj0pu3psvdqi85cuv.apps.googleusercontent.com")
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val googleAcc = GoogleSignIn.getLastSignedInAccount(this)
        if (googleAcc != null) {
            startActivity(Intent(this, (MainActivity::class.java)))
        }

        pref = getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)

        var resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    handleSignInResults(task)
                }
            }


        signInButton.setOnClickListener {
            val signInIntent = googleSignInClient!!.signInIntent
            resultLauncher.launch(signInIntent)
        }
    }

    private fun handleSignInResults(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.result
            with(pref.edit()) {
                this.putInt(Constants.ROLE_ID, 1)
                this.putString(Constants.USER_NAME, account.displayName)
                this.putString(Constants.USER_PHOTO_URL, account.photoUrl.toString())
                this.apply()
            }

            firebaseAuthWithGoogle(account);
            //loadDbFromFirebase(account)
        } catch (e: Exception) {
            Log.w("SIGN-IN", "signInResult:failed code=" + e.message);
            updateUI(null);
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth = FirebaseAuth.getInstance()
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user: FirebaseUser? = mAuth!!.currentUser
                        //storeDbToFirebase(account)
                        loadDbFromFirebase(account)
                    } else {
                        Toast.makeText(
                            this,
                            "Greška kod povezivanja sa Firebaseom",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
    }

    //Stavit pohranu na klik, pitanje kod sign outa
    private fun loadDbFromFirebase(account: GoogleSignInAccount) {
        findViewById<View>(R.id.loginPB).visibility=View.VISIBLE

        val auth = Firebase.auth
        val user = auth.currentUser

        val inFileName = getDatabasePath("baza TM").toString();
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        val dbRef = storageRef.child("databases/${user!!.uid}/baza TM")
        val localFile = File(inFileName)

        dbRef.getFile(localFile).addOnFailureListener {
            //ne postoji baza, izbrisi postojecu
            DatabaseHelper(this).izbrisiSveTabliceIAlarme()
            findViewById<View>(R.id.loginPB).visibility=View.GONE
            Toast.makeText(this, "Nisu pronađeni podaci povezani sa ovim računom!", Toast.LENGTH_SHORT).show()
            updateUI(account);
        }.addOnSuccessListener {
            Toast.makeText(this, "Preuzeti podaci sa clouda!", Toast.LENGTH_SHORT).show()
            updateUI(account);
        }
    }

    private fun storeDbToFirebase(account: GoogleSignInAccount) {
        val auth = Firebase.auth
        val user = auth.currentUser

        val inFileName = getDatabasePath("baza TM").toString();
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        val dbRef = storageRef.child("databases/${user!!.uid}/baza TM")
        var file = Uri.fromFile(File(inFileName))
        val uploadTask = dbRef.putFile(file)

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener {
            Toast.makeText(this, "Greška kod uploada podataka", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener { taskSnapshot ->
            updateUI(account);
            Toast.makeText(this, "Uspješan upload na cloud", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            Toast.makeText(this, "Pozdrav, " + account.displayName + "!", Toast.LENGTH_SHORT).show()

            startActivity(
                Intent(this, MainActivity::class.java)
            )
            //accountTV.text = account.displayName
            //signOutButton.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, "Greška kod prijave, pokušajte ponovo!", Toast.LENGTH_SHORT).show()
        }
    }


}