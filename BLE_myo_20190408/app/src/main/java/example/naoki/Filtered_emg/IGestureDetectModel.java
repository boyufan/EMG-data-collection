package example.naoki.Filtered_emg;

/**
 * Created by naoki on 15/04/16.
 */
public interface IGestureDetectModel {
    public void event(long eventTime,byte[] data);
    public void setAction(IGestureDetectAction action);
    public void action();
}
