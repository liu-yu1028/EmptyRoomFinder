import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emptyroomfinder.ClassroomScheduleRepository
import com.example.emptyroomfinder.ClassroomScheduleViewModel/**
 * 負責實例化ClassroomScheduleViewModel並注入Repository依賴
 * 因為 ViewModel 預設的構造函數不支援傳入參數(如 Repository)
 * 所以必須透過實作ViewModelProvider.Factory來達成「依賴注入」
 */
class ClassroomScheduleViewModelFactory(private val repository: ClassroomScheduleRepository) : ViewModelProvider.Factory {
    //系統回調，用來建立ViewModel實例
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        //檢查傳入的class類型是否為我們指定的ClassroomScheduleViewModel
        if (modelClass.isAssignableFrom(ClassroomScheduleViewModel::class.java)) {
            // @Suppress("UNCHECKED_CAST")告訴編譯器我們確認這個轉型是安全的
            @Suppress("UNCHECKED_CAST")
            // 建立包含repository參數的ViewModel實例
            return ClassroomScheduleViewModel(repository) as T
        }
        // 如果請求的類別不符合，則拋出異常
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}