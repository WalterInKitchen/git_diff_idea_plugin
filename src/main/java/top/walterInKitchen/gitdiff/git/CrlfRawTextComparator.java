package top.walterInKitchen.gitdiff.git;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;

import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CrlfRawTextComparator extends RawTextComparator {
    public static RawTextComparator INSTANCE = new CrlfRawTextComparator();

    @Override
    public boolean equals(RawText a, int ai, RawText b, int bi) {
        String aTxt = remoteCarriageReturn(a.getString(ai));
        String bTxt = remoteCarriageReturn(b.getString(bi));
        return aTxt.equals(bTxt);
    }

    private String remoteCarriageReturn(String txt) {
        byte[] bytes = txt.getBytes(StandardCharsets.UTF_8);
        int size = bytes.length;
        if (size == 0) {
            return "";
        }

        int start = 0;
        if (bytes[0] == '\r') {
            size--;
            start++;
        }
        if (bytes.length > 1 && bytes[bytes.length - 1] == '\r') {
            size--;
        }
        if (size <= 0) {
            return "";
        }

        byte[] noCrlfBytes = new byte[size];
        System.arraycopy(bytes, start, noCrlfBytes, 0, size);

        return new String(noCrlfBytes);
    }

    @Override
    protected int hashRegion(byte[] raw, int ptr, int end) {
        int hash = 5381;
        for (; ptr < end; ptr++)
            hash = ((hash << 5) + hash) + (raw[ptr] & 0xff);
        return hash;
    }
}
