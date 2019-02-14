package ro.edi.xbnr.data.db.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.*

interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(obj: T): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    @Transaction
    fun insert(objList: List<T>): List<Long>

    @Update
    fun update(obj: T)

    @Update
    @Transaction
    fun update(objList: List<T>)

    @Transaction
    fun insertOrUpdate(obj: T) {
        val id = insert(obj)
        if (id == -1L) update(obj)
    }

    @Delete
    fun delete(obj: T)

    @Delete
    @Transaction
    fun delete(objList: List<T>)

    fun <T> LiveData<T>.getDistinct(): LiveData<T> {
        var lastValue: Any? = Any()
        return MediatorLiveData<T>().apply {
            addSource(this@getDistinct) {
                if (it != lastValue) {
                    lastValue = it
                    postValue(it)
                }
            }
        }
    }

//    // alternative
//    fun <T> LiveData<T>.getDistinct(): LiveData<T> {
//        val distinctLiveData = MediatorLiveData<T>()
//        distinctLiveData.addSource(this, object : Observer<T> {
//            private var initialized = false
//            private var lastObj: T? = null
//            override fun onChanged(obj: T?) {
//                if (!initialized) {
//                    initialized = true
//                    lastObj = obj
//                    distinctLiveData.postValue(lastObj)
//                } else if ((obj == null && lastObj != null) || obj != lastObj) {
//                    lastObj = obj
//                    distinctLiveData.postValue(lastObj)
//                }
//            }
//        })
//        return distinctLiveData
//    }
}