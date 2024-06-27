package com.example.inquizitive.data.error

import com.example.inquizitive.data.common.ResultWrapper
import java.lang.ref.WeakReference

abstract class CallbackWrapper<T>(view: IErrorCallback, resultWrapper: ResultWrapper<T>) {
    private val weakReference: WeakReference<IErrorCallback>?

    protected abstract fun onSuccess(data: T)

    /** Descendant viewModels that want to be notified on specific endpoints whether to switch to their error layout should override this, and update BaseViewModel's showErrorLayout var. **/
//    protected open fun onDisplayErrorLayout(errorResponse: ErrorResponse?) {}

    init {
        weakReference = WeakReference(view)

        val viewWeakRef = weakReference.get()

        viewWeakRef?.let {

            resultWrapper.result?.let { result ->
                onSuccess(result)
            }

            resultWrapper.error?.let { error ->
                when (error) {
                    is ResultWrapper.NetworkError -> {
                        viewWeakRef.onNetworkError()
                        //onDisplayErrorLayout(null)
                    }

                    is ResultWrapper.NetworkTimeOutError -> {
                        viewWeakRef.onTimeout()
                        //onDisplayErrorLayout(null)
                    }

                    is ResultWrapper.SessionExpired -> {
                        viewWeakRef.onSessionExpired()
                    }

                    is ResultWrapper.GenericError -> {
                        viewWeakRef.onGenericError(error.message, null)
                        //onDisplayErrorLayout(ErrorResponse(error.message, false))
                    }

                    is ResultWrapper.HttpError -> {
                        viewWeakRef.onGenericError(error.message, error.validationErrors)
                        //onDisplayErrorLayout(ErrorResponse(error.message, false))
                    }
                }
            }
        }
    }
}