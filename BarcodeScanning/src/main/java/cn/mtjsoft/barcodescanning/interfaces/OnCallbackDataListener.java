package cn.mtjsoft.barcodescanning.interfaces;

/**
 * @author mtj
 * @date 2022/1/24
 * @desc
 * @email mtjsoft3@gmail.com
 */
public interface OnCallbackDataListener<T> {
    /**
     * @param data
     */
    void onCall(T data);
}
