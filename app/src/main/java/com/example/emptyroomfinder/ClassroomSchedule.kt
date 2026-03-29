
package com.example.emptyroomfinder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classroom_schedules")
data class                                                                                                                  ClassroomSchedule(
    @PrimaryKey
    // 教室名稱作為Unique ID
    val classroom: String,

    @ColumnInfo(name = "Mon_1") val mon1: String?,
    @ColumnInfo(name = "Mon_2") val mon2: String?,
    @ColumnInfo(name = "Mon_3") val mon3: String?,
    @ColumnInfo(name = "Mon_4") val mon4: String?,
    @ColumnInfo(name = "Mon_N") val monN: String?,
    @ColumnInfo(name = "Mon_5") val mon5: String?,
    @ColumnInfo(name = "Mon_6") val mon6: String?,
    @ColumnInfo(name = "Mon_7") val mon7: String?,
    @ColumnInfo(name = "Mon_8") val mon8: String?,

    @ColumnInfo(name = "Tue_1") val tue1: String?,
    @ColumnInfo(name = "Tue_2") val tue2: String?,
    @ColumnInfo(name = "Tue_3") val tue3: String?,
    @ColumnInfo(name = "Tue_4") val tue4: String?,
    @ColumnInfo(name = "Tue_N") val tueN: String?,
    @ColumnInfo(name = "Tue_5") val tue5: String?,
    @ColumnInfo(name = "Tue_6") val tue6: String?,
    @ColumnInfo(name = "Tue_7") val tue7: String?,
    @ColumnInfo(name = "Tue_8") val tue8: String?,

    @ColumnInfo(name = "Wed_1") val wed1: String?,
    @ColumnInfo(name = "Wed_2") val wed2: String?,
    @ColumnInfo(name = "Wed_3") val wed3: String?,
    @ColumnInfo(name = "Wed_4") val wed4: String?,
    @ColumnInfo(name = "Wed_N") val wedN: String?,
    @ColumnInfo(name = "Wed_5") val wed5: String?,
    @ColumnInfo(name = "Wed_6") val wed6: String?,
    @ColumnInfo(name = "Wed_7") val wed7: String?,
    @ColumnInfo(name = "Wed_8") val wed8: String?,

    @ColumnInfo(name = "Thu_1") val thu1: String?,
    @ColumnInfo(name = "Thu_2") val thu2: String?,
    @ColumnInfo(name = "Thu_3") val thu3: String?,
    @ColumnInfo(name = "Thu_4") val thu4: String?,
    @ColumnInfo(name = "Thu_N") val thuN: String?,
    @ColumnInfo(name = "Thu_5") val thu5: String?,
    @ColumnInfo(name = "Thu_6") val thu6: String?,
    @ColumnInfo(name = "Thu_7") val thu7: String?,
    @ColumnInfo(name = "Thu_8") val thu8: String?,

    @ColumnInfo(name = "Fri_1") val fri1: String?,
    @ColumnInfo(name = "Fri_2") val fri2: String?,
    @ColumnInfo(name = "Fri_3") val fri3: String?,
    @ColumnInfo(name = "Fri_4") val fri4: String?,
    @ColumnInfo(name = "Fri_N") val friN: String?,
    @ColumnInfo(name = "Fri_5") val fri5: String?,
    @ColumnInfo(name = "Fri_6") val fri6: String?,
    @ColumnInfo(name = "Fri_7") val fri7: String?,
    @ColumnInfo(name = "Fri_8") val fri8: String?,

    @ColumnInfo(name = "ClassroomType") val classroomType: String?
)
