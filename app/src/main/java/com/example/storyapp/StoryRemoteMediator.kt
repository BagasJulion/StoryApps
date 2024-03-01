package com.example.storyapp

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.storyapp.Response.ListStoryItem
import com.example.storyapp.database.StoryDatabase
import com.example.storyapp.login.LoginPreferences
import com.example.storyapp.retrofit.api.ApiConfig
import kotlinx.coroutines.flow.first


@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator (
    private val database: StoryDatabase,
    private val preference: LoginPreferences
): RemoteMediator<Int, ListStoryItem>(){

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryItem>
    ): MediatorResult {

        val page = when (loadType) {
            LoadType.REFRESH -> getRemoteKeyClosestToCurrentPosition(state)?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            LoadType.PREPEND -> getRemoteKeyForFirstItem(state)?.prevKey ?: return MediatorResult.Success(endOfPaginationReached = false)
            LoadType.APPEND -> getRemoteKeyForLastItem(state)?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = false)
        }

        return try {
            val getToken = preference.getSession().first()
            val apiService = ApiConfig.getApiService(getToken.token)
            val responseData = apiService.getStories(page, state.config.pageSize)
            val stories = responseData.listStory.map { story ->
                ListStoryItem(
                    story.photoUrl,
                    story.createdAt,
                    story.name,
                    story.description,
                    story.lon,
                    story.id,
                    story.lat
                )
            }
            val endOfPaginationReached = stories.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().deleteRemoteKeys()
                    database.storyDao().deleteAll()
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = stories.map {
                    RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }

                database.remoteKeysDao().insertAll(keys)
                database.storyDao().insertStory(stories)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }
    }


    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ListStoryItem>): RemoteKeys?{
        return state.pages.lastOrNull{it.data.isNotEmpty()}?.data?.lastOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeysDao().getRemoteKeysId(id)
            }
        }
    }


    companion object{
        const val INITIAL_PAGE_INDEX = 1
    }

}