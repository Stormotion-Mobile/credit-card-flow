package io.stormotion.creditcardflow.credit_card_flow

import android.os.Parcel
import android.os.Parcelable
import android.view.View

class SavedState : View.BaseSavedState {
    lateinit var cardToSave: CreditCard

    constructor(superState: Parcelable) : super(superState)

    constructor(`in`: Parcel) : super(`in`) {
        this.cardToSave = `in`.readSerializable() as CreditCard
    }

    override fun writeToParcel(`out`: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeSerializable(this.cardToSave)
    }

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

    }    }