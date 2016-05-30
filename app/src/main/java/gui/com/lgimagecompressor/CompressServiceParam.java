package gui.com.lgimagecompressor;

import android.os.Parcel;
import android.os.Parcelable;

public class CompressServiceParam implements Parcelable {

    private int outWidth;
    private int outHeight;
    private int maxFileSize;
    private String srcImageUri;

    public CompressServiceParam() {
    }

    protected CompressServiceParam(Parcel in) {
        outWidth = in.readInt();
        outHeight = in.readInt();
        maxFileSize = in.readInt();
        srcImageUri = in.readString();
    }

    public static final Creator<CompressServiceParam> CREATOR = new Creator<CompressServiceParam>() {
        @Override
        public CompressServiceParam createFromParcel(Parcel in) {
            return new CompressServiceParam(in);
        }

        @Override
        public CompressServiceParam[] newArray(int size) {
            return new CompressServiceParam[size];
        }
    };

    public int getOutWidth() {
        return outWidth;
    }

    public void setOutWidth(int outWidth) {
        this.outWidth = outWidth;
    }

    public int getOutHeight() {
        return outHeight;
    }

    public void setOutHeight(int outHeight) {
        this.outHeight = outHeight;
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public String getSrcImageUri() {
        return srcImageUri;
    }

    public void setSrcImageUri(String srcImageUri) {
        this.srcImageUri = srcImageUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(outWidth);
        dest.writeInt(outHeight);
        dest.writeInt(maxFileSize);
        dest.writeString(srcImageUri);
    }
}