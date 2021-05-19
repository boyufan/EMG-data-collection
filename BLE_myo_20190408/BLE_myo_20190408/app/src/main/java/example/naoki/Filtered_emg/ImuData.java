package example.naoki.Filtered_emg;



public class ImuData{
    public static String format(double[] data) {
        StringBuffer builder = new StringBuffer();
        for (double d : data)
            builder.append(String.format("%+.3f", d)).append(" ");
        return builder.toString();
    }
}
