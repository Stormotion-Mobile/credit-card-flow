package io.stormotion.creditcardflow.credit_card_flow

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import io.stormotion.creditcardflow.CardFlowState
import io.stormotion.creditcardflow.CreditCard
import java.io.Serializable

class SavedState : View.BaseSavedState {
    lateinit var cardFlowDataToSave: CardFlowData

    constructor(superState: Parcelable) : super(superState)

    constructor(`in`: Parcel) : super(`in`) {
        this.cardFlowDataToSave = `in`.readSerializable() as CardFlowData
    }

    override fun writeToParcel(`out`: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeSerializable(this.cardFlowDataToSave)
    }

    data class CardFlowData(val state: CardFlowState, val creditCard: CreditCard): Serializable

    //required field that makes Parcelables from a Parcel
    companion object {
        val CREATOR =
                object : Parcelable.Creator<SavedState> {
                    override fun createFromParcel(`in`: Parcel): SavedState {
                        return SavedState(`in`)
                    }

                    public override fun newArray(size: Int): Array<SavedState> {
                        return Array(size, { SavedState(Parcel.obtain()) })
                    }
                };

    }
}