package com.yupi.yupicodesandbox.unsafe;

import java.util.ArrayList;
import java.util.List;

public class main {
    public static void main(String[] args) {
        List<byte[]> bytes = new ArrayList<>();
        while (true){
            bytes.add(new byte[10000]);
        }
    }
}
