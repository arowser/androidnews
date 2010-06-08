/* Copyright (c) 2009 Matthias Käppler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vn.evolus.news.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import vn.evolus.news.model.Image;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

import com.google.common.collect.MapMaker;

/**
 * <p>
 * A simple 2-level cache for bitmap images consisting of a small and fast
 * in-memory cache (1st level cache) and a slower but bigger disk cache (2nd
 * level cache). For second level caching, the application's cache directory
 * will be used. Please note that Android may at any point decide to wipe that
 * directory.
 * </p>
 * <p>
 * When pulling from the cache, it will first attempt to load the image from
 * memory. If that fails, it will try to load it from disk. If that succeeds,
 * the image will be put in the 1st level cache and returned. Otherwise it's a
 * cache miss, and the caller is responsible for loading the image from
 * elsewhere (probably the Internet).
 * </p>
 * <p>
 * Pushes to the cache are always write-through (i.e., the image will be stored
 * both on disk and in memory).
 * </p>
 * 
 * @author Matthias Kaeppler
 */
public class ImageCache implements Map<String, Bitmap> {
	
	private static ImageCache instance;
	
    private int cachedImageQuality = 75;

    // private int firstLevelCacheSize = 10;

    private String secondLevelCacheDir;

    private Map<String, Bitmap> cache;

    private CompressFormat compressedImageFormat = CompressFormat.JPEG;
    
    private ContentResolver cr;

    private ImageCache(Context context, int initialCapacity, int concurrencyLevel) {
        this.cache = new MapMaker().initialCapacity(initialCapacity).concurrencyLevel(
            concurrencyLevel).weakValues().makeMap();
        this.secondLevelCacheDir = Environment.getExternalStorageDirectory().getAbsolutePath() //context.getApplicationContext().getCacheDir()
                + "/droidnews/imagecache";
        new File(secondLevelCacheDir).mkdirs();
        
        this.cr = context.getContentResolver();
    }
    
    public static synchronized ImageCache createInstance(Context context, int initialCapacity, int concurrencyLevel) {
    	instance = new ImageCache(context, initialCapacity, concurrencyLevel);
    	return instance;
    }
    
    public static synchronized ImageCache getInstance() {
    	return instance;
    }

    // /**
    // * Changing the in-memory cache size will invalidate the cache if new
    // value
    // * < old value.
    // *
    // * @param firstLevelCacheSize
    // * maximum number of objects that should be held in memory at any
    // * time
    // */
    // public void setFirstLevelCacheSize(int firstLevelCacheSize) {
    // this.firstLevelCacheSize = firstLevelCacheSize;
    // // invalidate the cache
    // if (firstLevelCacheSize < this.firstLevelCacheSize) {
    // this.clear();
    // }
    // }
    //
    // public int getFirstLevelCacheSize() {
    // return firstLevelCacheSize;
    // }

    /**
     * @param cachedImageQuality
     *        the quality of images being compressed and written to disk (2nd
     *        level cache) as a number in [0..100]
     */
    public void setCachedImageQuality(int cachedImageQuality) {
        this.cachedImageQuality = cachedImageQuality;
    }

    public int getCachedImageQuality() {
        return cachedImageQuality;
    }

    public synchronized Bitmap get(Object key) {
        String imageUrl = (String) key;
        Bitmap bitmap = cache.get(imageUrl);

        if (bitmap != null) {
            // 1st level cache hit (memory)
            return bitmap;
        }

        File imageFile = getImageFile(imageUrl);
        if (imageFile.exists()) {
            // 2nd level cache hit (disk)
        	try {
        		bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        	} catch (Throwable e) {}
        	
            if (bitmap == null) {
                // treat decoding errors as a cache miss
                return null;
            }
            cache.put(imageUrl, bitmap);
            return bitmap;
        }

        // cache miss
        return null;
    }

    public Bitmap put(String imageUrl, Bitmap image) {
        File imageFile = getImageFile(imageUrl);
        try {
            imageFile.createNewFile();
            FileOutputStream ostream = new FileOutputStream(imageFile);
            image.compress(compressedImageFormat, cachedImageQuality, ostream);
            ostream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cache.put(imageUrl, image);
    }

    public void putAll(Map<? extends String, ? extends Bitmap> t) {
        throw new UnsupportedOperationException();
    }

    public boolean containsKey(Object key) {
        return cache.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return cache.containsValue(value);
    }

    public Bitmap remove(Object key) {
        return cache.remove(key);
    }

    public Set<String> keySet() {
        return cache.keySet();
    }

    public Set<java.util.Map.Entry<String, Bitmap>> entrySet() {
        return cache.entrySet();
    }

    public int size() {
        return cache.size();
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public void clear() {
        cache.clear();
    }

    public Collection<Bitmap> values() {
        return cache.values();
    }

    private File getImageFile(String imageUrl) {
        return new File(getCacheFileName(imageUrl));
    }
    
    public static void downloadImage(String imageUrl) throws IOException {
    	URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        // save to cache folder
    	File imageFile = new File(getCacheFileName(imageUrl));
        imageFile.createNewFile();
        FileOutputStream os = new FileOutputStream(imageFile);	                
        StreamUtils.writeStream(is, os);
        os.close();
    }
    
    public static String getCacheFileName(String imageUrl) {
    	Image image = Image.load(imageUrl, instance.cr);
    	return instance.secondLevelCacheDir + "/" + (image != null ? image.getId() : "0");
    }
}