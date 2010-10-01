package vn.evolus.droidreader.content.criteria;

import java.util.ArrayList;
import java.util.List;

import vn.evolus.droidreader.Constants;
import vn.evolus.droidreader.content.ItemCriteria;
import vn.evolus.droidreader.model.Item;
import vn.evolus.droidreader.model.Item.Items;
import android.net.Uri;

public class ItemOfTag implements ItemCriteria {
	public static final int ALL_TAGS = -1;
	
	public static final byte OLDER = 1;
	public static final byte NEWER = 2;
	
	public int tagId = ALL_TAGS;
	public boolean onlyUnreadItems = false;
	public Item compareToItem = null;
	public byte comparision = OLDER;
	public int maxItems = Constants.MAX_ITEMS;
	
	public ItemOfTag(int tagId, boolean onlyUnreadItems) {
		this(tagId, onlyUnreadItems, null, OLDER, Constants.MAX_ITEMS);
	}
	
	public ItemOfTag(int tagId, boolean onlyUnreadItems, Item compareToItem,
			byte comparision, int maxItems) {
		super();
		this.tagId = tagId;
		this.onlyUnreadItems = onlyUnreadItems;
		this.compareToItem = compareToItem;
		this.comparision = comparision;
		this.maxItems = maxItems;
	}

	@Override
	public String getSelection() {
		StringBuilder sb = new StringBuilder();
		if (tagId != ALL_TAGS) {
			sb.append(Items.TAG_ID + "=?");
		}		
		if (onlyUnreadItems) {
			if (sb.length() > 0) {
				sb.append(" AND ");				
			}
			sb.append(Items.READ + "=0");
		}
		if (compareToItem != null) {
			if (sb.length() > 0) {
				sb.append(" AND ");
			}
			if (comparision == OLDER) {
				sb.append("(" + Items.UPDATE_TIME + "<? OR (" + Items.UPDATE_TIME + "=? AND (" + 
						Items.PUB_DATE + "<? OR (" + Items.PUB_DATE + " =? AND " + Items.ID + ">?))))");
			} else if (comparision == NEWER) {
				sb.append("(" + Items.UPDATE_TIME + ">? OR (" + Items.UPDATE_TIME + "=? AND (" + 
						Items.PUB_DATE + ">? OR (" + Items.PUB_DATE + " =? AND " + Items.ID + "<?))))");
			}
		}
		if (sb.length() == 0) return null;
		else return sb.toString();
	}

	@Override
	public String[] getSelectionArgs() {
		List<String> args = new ArrayList<String>();		
		if (tagId != ALL_TAGS) {
			args.add(String.valueOf(tagId));
		}
		
		if (compareToItem != null) {
			args.add(String.valueOf(compareToItem.updateTime));
			args.add(String.valueOf(compareToItem.updateTime));
			args.add(String.valueOf(compareToItem.pubDate.getTime()));
			args.add(String.valueOf(compareToItem.pubDate.getTime()));
			args.add(String.valueOf(compareToItem.id));
		}
		return args.toArray(new String[0]);
	}
	
	@Override
	public String getOrderBy() {
		if (comparision == OLDER) {
			return Items.UPDATE_TIME + " DESC, " +
				Items.PUB_DATE + " DESC, " +
				Items.ID + " ASC";
		} else {
			return Items.UPDATE_TIME + " ASC, " +
				Items.PUB_DATE + " ASC, " +
				Items.ID + " DESC";
		}
	}

	@Override
	public Uri getContentUri() {		
		return (tagId == ALL_TAGS ? Items.limit(maxItems) : Items.hasTagAndLimit(maxItems));
	}
}
