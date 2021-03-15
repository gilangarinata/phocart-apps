package studio.vim.phocart.utils


/**
 * Created by Gilang Arinata on 10/01/21.
 * https://github.com/gilangarinata/
 */
sealed class Resource<T>(
        val data: T? = null,
        val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(code : Int, message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}