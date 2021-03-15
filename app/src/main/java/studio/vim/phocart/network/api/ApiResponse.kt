package studio.vim.phocart.network.api

data class ApiResponse(
        val code : Int,
        val message : String,
        val path : String?
)