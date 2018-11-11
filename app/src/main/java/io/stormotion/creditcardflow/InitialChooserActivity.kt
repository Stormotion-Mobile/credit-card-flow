package io.stormotion.creditcardflow

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class InitialChooserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_chooser)
    }


    fun addCreditCard(view: View) {
        startActivity(AddEditCardActivity.getInstance(this, null))
    }

    fun editCreditCard(view: View) {
        startActivity(AddEditCardActivity.getInstance(this,
                CreditCard(
                        "5454 5454 5454 5454",
                        "Someone",
                        "123",
                        "10/26"
                )
        ))
    }
}
