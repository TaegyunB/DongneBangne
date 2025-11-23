import java.util.*;

class Solution {
    public boolean solution(String[] phone_book) {

        int[] list = new int[phone_book.length];

        for (int i=0; i<phone_book.length; i++) {
            int num = Integer.parseInt(phone_book[i]);

            list[i] = num;
        }

        Arrays.sort(list);

        boolean result = false;

        for (int i=0; i<list.length-1; i++) {
            String prefix = Integer.toString(list[i]);
            int prefix_size = prefix.length();

            for (int j=i+1; j<list.length; j++) {
                String target = Integer.toString(j);
                if (target.substring(0, prefix_size).equals(prefix)) {
                    result = true;
                }

                if (result) {
                    break;
                }
            }

            if (result) {
                break;
            }
        }

        return false;
    }
}