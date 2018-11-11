package io.stormotion.creditcardflow

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import io.stormotion.creditcardflow.credit_card_flow.CreditCard

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
                        null,
                        "5454 5454 5454 5454",
                        "Someone",
                        null,
                        "123",
                        "10/26",
                        null,
                        null)
        ))
    }
}
