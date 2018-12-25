package io.stormotion.creditcardflow.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import io.stormotion.creditcardflow.sample.AddEditCardFragment.Companion.ADD_EDIT_CARD_CREDIT_CARD_EXTRA


class AddEditCardActivity : BaseSettingsActivity() {

    companion object {
        fun getInstance(activity: Activity, creditCard: CreditCard?) =
                Intent(activity, AddEditCardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    putExtra(ADD_EDIT_CARD_CREDIT_CARD_EXTRA, creditCard)
                }
    }


    override val menuRes = R.menu.add_edit_card_menu
    override val toolbarTitleRes
        get() = if (intent.extras != null && intent.extras.getSerializable(ADD_EDIT_CARD_CREDIT_CARD_EXTRA) as CreditCard? != null)
            R.string.credit_cards_add_edit_edit_title
        else
            R.string.credit_cards_add_edit_add_title


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val card = intent.getSerializableExtra(ADD_EDIT_CARD_CREDIT_CARD_EXTRA) as CreditCard?

        val currentFragment = (supportFragmentManager.findFragmentById(R.id.contentFrame) as AddEditCardFragment?)?.let {
            it
        } ?: {
            val settingsFragment = AddEditCardFragment.getInstance(card)
            addFragmentToActivity(supportFragmentManager, settingsFragment, R.id.contentFrame)
            settingsFragment
        }.invoke()
        AddEditCardPresenter(currentFragment)

        with(supportActionBar!!) {
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.contentFrame)
        (currentFragment as AddEditCardFragment?)?.onBackPressed(this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.contentFrame)
        currentFragment!!.onOptionsItemSelected(item)
        return true
    }
}
