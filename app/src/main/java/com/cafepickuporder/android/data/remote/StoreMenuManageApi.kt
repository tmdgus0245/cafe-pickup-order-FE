package com.cafepickuporder.android.data.remote

import com.cafepickuporder.android.data.request.MenuCategoryManageRequest
import com.cafepickuporder.android.data.request.MenuManageRequest
import com.cafepickuporder.android.data.request.MenuOptionGroupManageRequest
import com.cafepickuporder.android.data.request.MenuOptionManageRequest
import com.cafepickuporder.android.data.response.MenuCategoryResponse
import com.cafepickuporder.android.data.response.MenuDetailResponse
import com.cafepickuporder.android.data.response.MenuResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface StoreMenuManageApi {

    @GET("/api/stores/{storeId}/manage/categories")
    suspend fun getCategories(
        @Path("storeId") storeId: Long,
        @Header("Authorization") authorization: String
    ): List<MenuCategoryResponse>

    @POST("/api/stores/{storeId}/manage/categories")
    suspend fun createCategory(
        @Path("storeId") storeId: Long,
        @Header("Authorization") authorization: String,
        @Body request: MenuCategoryManageRequest
    ): MenuCategoryResponse

    @PATCH("/api/stores/{storeId}/manage/categories/{categoryId}")
    suspend fun updateCategory(
        @Path("storeId") storeId: Long,
        @Path("categoryId") categoryId: Long,
        @Header("Authorization") authorization: String,
        @Body request: MenuCategoryManageRequest
    ): MenuCategoryResponse

    @DELETE("/api/stores/{storeId}/manage/categories/{categoryId}")
    suspend fun deleteCategory(
        @Path("storeId") storeId: Long,
        @Path("categoryId") categoryId: Long,
        @Header("Authorization") authorization: String
    )

    @POST("/api/stores/{storeId}/manage/menus")
    suspend fun createMenu(
        @Path("storeId") storeId: Long,
        @Header("Authorization") authorization: String,
        @Body request: MenuManageRequest
    ): MenuResponse

    @PATCH("/api/stores/{storeId}/manage/menus/{menuId}")
    suspend fun updateMenu(
        @Path("storeId") storeId: Long,
        @Path("menuId") menuId: Long,
        @Header("Authorization") authorization: String,
        @Body request: MenuManageRequest
    ): MenuResponse

    @DELETE("/api/stores/{storeId}/manage/menus/{menuId}")
    suspend fun deleteMenu(
        @Path("storeId") storeId: Long,
        @Path("menuId") menuId: Long,
        @Header("Authorization") authorization: String
    )

    @POST("/api/stores/{storeId}/manage/menus/{menuId}/option-groups")
    suspend fun createOptionGroup(
        @Path("storeId") storeId: Long,
        @Path("menuId") menuId: Long,
        @Header("Authorization") authorization: String,
        @Body request: MenuOptionGroupManageRequest
    ): MenuDetailResponse

    @PATCH("/api/stores/{storeId}/manage/menus/{menuId}/option-groups/{optionGroupId}")
    suspend fun updateOptionGroup(
        @Path("storeId") storeId: Long,
        @Path("menuId") menuId: Long,
        @Path("optionGroupId") optionGroupId: Long,
        @Header("Authorization") authorization: String,
        @Body request: MenuOptionGroupManageRequest
    ): MenuDetailResponse

    @DELETE("/api/stores/{storeId}/manage/menus/{menuId}/option-groups/{optionGroupId}")
    suspend fun deleteOptionGroup(
        @Path("storeId") storeId: Long,
        @Path("menuId") menuId: Long,
        @Path("optionGroupId") optionGroupId: Long,
        @Header("Authorization") authorization: String
    )

    @POST("/api/stores/{storeId}/manage/menus/{menuId}/option-groups/{optionGroupId}/options")
    suspend fun createOption(
        @Path("storeId") storeId: Long,
        @Path("menuId") menuId: Long,
        @Path("optionGroupId") optionGroupId: Long,
        @Header("Authorization") authorization: String,
        @Body request: MenuOptionManageRequest
    ): MenuDetailResponse

    @PATCH("/api/stores/{storeId}/manage/menus/{menuId}/option-groups/{optionGroupId}/options/{optionId}")
    suspend fun updateOption(
        @Path("storeId") storeId: Long,
        @Path("menuId") menuId: Long,
        @Path("optionGroupId") optionGroupId: Long,
        @Path("optionId") optionId: Long,
        @Header("Authorization") authorization: String,
        @Body request: MenuOptionManageRequest
    ): MenuDetailResponse

    @DELETE("/api/stores/{storeId}/manage/menus/{menuId}/option-groups/{optionGroupId}/options/{optionId}")
    suspend fun deleteOption(
        @Path("storeId") storeId: Long,
        @Path("menuId") menuId: Long,
        @Path("optionGroupId") optionGroupId: Long,
        @Path("optionId") optionId: Long,
        @Header("Authorization") authorization: String
    )
}
