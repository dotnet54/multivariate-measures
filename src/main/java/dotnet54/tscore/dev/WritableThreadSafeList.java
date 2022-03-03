package dotnet54.tscore.dev;

import com.kitfox.svg.A;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WritableThreadSafeList<T> {

    /*

    does not support string escaping
    should support THREAD safety
    currently using a synchronizedList
    might try to experiment with other thread safe collections

     */

    private File file;
    private String fileName;
    private FileWriter fw;
    private BufferedWriter bw;
    private List<T> records;

    public WritableThreadSafeList(int defaultCapacity){
        records = Collections.synchronizedList(new ArrayList<>(defaultCapacity));
    }

    public void add(T record){
        records.add(record);
    }

    public List<T> records(){
        return records;
    }

    public void open(String fileName, boolean append){
        try {
            file = new File(fileName);
            fw = new FileWriter(file, append);
            bw = new BufferedWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String fileName, boolean append){
        try {
            open(fileName, append);

            int size = records.size();
            for (int i = 0; i < size; i++) {
                T record = records.get(i);
                bw.write(record.toString());
                bw.write("\n");
            }

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
