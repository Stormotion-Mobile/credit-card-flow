package io.stormotion.creditcardflow.mvp

import android.support.v4.app.Fragment

abstract class BaseFragment<T : BasePresenter> : Fragment() {
    protected lateinit var mPresenter: T
}

