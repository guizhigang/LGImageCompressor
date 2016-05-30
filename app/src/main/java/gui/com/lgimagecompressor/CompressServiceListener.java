package gui.com.lgimagecompressor;

import java.util.ArrayList;

/**
 * Created by guizhigang on 16/5/28.
 */
public interface CompressServiceListener {
    void onCompressServiceStart();
    void onCompressServiceEnd(ArrayList<LGImgCompressor.CompressResult> compressResults);
}
