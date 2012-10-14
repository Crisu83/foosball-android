package org.cniska.foosball.android;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This activity lists the player statistics.
 */
public class StatisticsActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	// Enumerables
	// ----------------------------------------

	public enum SortColumn {
		PLAYER,
		GOALS,
		GOALS_AGAINST,
		GAMES_PLAYED,
		WINS,
		LOSSES,
		WIN_LOSS_RATIO,
		RATING
	}

	public enum SortDirection {
		ASCENDING,
		DESCENDING
	}

	// Inner classes
	// ----------------------------------------

	private class PlayerComparator implements Comparator<Player> {

		private SortColumn mColumn;
		private SortDirection mDirection;

		@Override
		public int compare(Player p1, Player p2) {
			if (mColumn != null) {
				switch (mColumn) {
					case GOALS:
						return compareInt(p1.getGoalsFor(), p2.getGoalsFor());
					case GOALS_AGAINST:
						return compareInt(p1.getGoalsAgainst(), p2.getGoalsAgainst());
					case GAMES_PLAYED:
						return compareInt(p1.gamesPlayed(), p2.gamesPlayed());
					case WINS:
						return compareInt(p1.getWins(), p2.getWins());
					case LOSSES:
						return compareInt(p1.getLosses(), p2.getLosses());
					case WIN_LOSS_RATIO:
						return compareFloat(p1.winLossRatio(), p2.winLossRatio());
					case RATING:
						return compareInt(p1.getRating(), p2.getRating());
					case PLAYER:
					default:
						return compareString(p1.getName(), p2.getName());
				}
			} else {
				return 0;
			}
		}

		/**
		 * Compares two strings and returns the result.
		 * @param value1 String 1.
		 * @param value2 String 2.
		 * @return The result.
		 */
		private int compareString(String value1, String value2) {
			if (mDirection == SortDirection.ASCENDING) {
				return value1.compareTo(value2);
			} else {
				return value2.compareTo(value1);
			}
		}

		/**
		 * Compares two integers and returns the result.
		 * @param value1 Integer 1.
		 * @param value2 Integer 2.
		 * @return The result.
		 */
		private int compareInt(int value1, int value2) {
			return compareFloat(value1, value2);
		}

		/**
		 * Compares two floats and returns the result.
		 * @param value1 Float 1.
		 * @param value2 Float 2.
		 * @return The result.
		 */
		private int compareFloat(float value1, float value2) {
			if (mDirection == SortDirection.ASCENDING && (value1 < value2)
					|| mDirection == SortDirection.DESCENDING && (value2 < value1)) {
				return 1;
			} else if (mDirection == SortDirection.ASCENDING && (value1 > value2)
					|| mDirection == SortDirection.DESCENDING && (value2 > value1)) {
				return -1;
			} else {
				return 0;
			}
		}

		/**
		 * Changes the sorting order for the comparator.
		 * @param column Sort column type.
		 */
		public void sortColumn(SortColumn column) {
			if (mDirection != null && column == this.mColumn) {
				mDirection = oppositeDirection();
			} else {
				mDirection = defaultDirection();
			}

			this.mColumn = column;
		}

		/**
		 * Returns the opposite sorting direction to the current.
		 * @return The direction.
		 */
		private SortDirection oppositeDirection() {
			return mDirection == SortDirection.ASCENDING ? SortDirection.DESCENDING : SortDirection.ASCENDING;
		}

		/**
		 * Returns the default sorting direction.
		 * @return The direction.
		 */
		public SortDirection defaultDirection() {
			return SortDirection.ASCENDING;
		}
	}

	// Static variables
	// ----------------------------------------

	private static final String TAG = "StatisticsActivity";

	private static final int LAYOUT_WEIGHT_PLAYER = 30;
	private static final int LAYOUT_WEIGHT_GOALS = 10;
	private static final int LAYOUT_WEIGHT_GOALS_AGAINST = 10;
	private static final int LAYOUT_WEIGHT_GAMES_PLAYED = 10;
	private static final int LAYOUT_WEIGHT_WINS = 10;
	private static final int LAYOUT_WEIGHT_LOSSES = 10;
	private static final int LAYOUT_WEIGHT_WIN_LOSS_RATIO = 10;
	private static final int LAYOUT_WEIGHT_RATING = 20;

	// Member variables
	// ----------------------------------------

	//private CursorAdapter mAdapter;

	private List<Player> mPlayers = new ArrayList<Player>();
	private PlayerComparator mComparator = new PlayerComparator();
	private TableLayout mLayout;

	// Methods
	// ----------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initActionBar(false, true);

		setContentView(R.layout.statistics);

		mLayout = (TableLayout) findViewById(R.id.table_statistics);

		/*
		String[] from = { Player.NAME, Player.GOALS_FOR, Player.GOALS_AGAINST, Player.WINS, Player.LOSSES, Player.RATING };
		int[] to = new int[] { R.id.column_name, R.id.column_goals, R.id.column_goals_against, R.id.column_wins, R.id.column_losses, R.id.column_rating };
		mAdapter = new SimpleCursorAdapter(this, R.layout.statistics_item, null, from, to, 0);
		setListAdapter(mAdapter);
		*/

		getSupportLoaderManager().initLoader(0, null, this);

		Logger.info(TAG, "Activity created.");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				startMainActivity();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { Player._ID, Player.NAME, Player.GOALS_FOR, Player.GOALS_AGAINST, Player.WINS, Player.LOSSES, Player.RATING };
		return new CursorLoader(getApplicationContext(), Player.CONTENT_URI, projection, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		//mAdapter.swapCursor(data);

		if (data.moveToFirst()) {
			int i = 0;
			while (!data.isAfterLast()) {
				mPlayers.add(cursorToPlayer(data));
				data.moveToNext();
				i++;
			}
		}

		addTableHeaderRow(mLayout);
		addTablePlayerRows(mLayout);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		//mAdapter.swapCursor(null);
	}

	private Player cursorToPlayer(Cursor cursor) {
		Player player = new Player();
		player.setId(cursor.getLong(0));
		player.setName(cursor.getString(1));
		player.setGoalsFor(cursor.getInt(2));
		player.setGoalsAgainst(cursor.getInt(3));
		player.setWins(cursor.getInt(4));
		player.setLosses(cursor.getInt(5));
		player.setRating(cursor.getInt(6));
		return player;
	}

	/**
	 * Creates the table header row and adds it to the given table layout.
	 * @param layout The table layout.
	 */
	private void addTableHeaderRow(TableLayout layout) {
		TableRow row = new TableRow(this);

		TextView headerPlayer = createTableHeaderCell(getResources().getString(R.string.table_header_player), LAYOUT_WEIGHT_PLAYER, Gravity.LEFT, 0);
		headerPlayer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.PLAYER);
			}
		});
		row.addView(headerPlayer);

		TextView headerGamesPlayed = createTableHeaderCell(getResources().getString(R.string.table_header_games_played), LAYOUT_WEIGHT_GAMES_PLAYED, Gravity.CENTER, 0);
		headerGamesPlayed.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.GAMES_PLAYED);
			}
		});
		row.addView(headerGamesPlayed);

		TextView headerWins = createTableHeaderCell(getResources().getString(R.string.table_header_wins), LAYOUT_WEIGHT_WINS, Gravity.CENTER, 0);
		headerWins.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.WINS);
			}
		});
		row.addView(headerWins);

		TextView headerLosses = createTableHeaderCell(getResources().getString(R.string.table_header_losses), LAYOUT_WEIGHT_LOSSES, Gravity.CENTER, 0);
		headerLosses.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.LOSSES);
			}
		});
		row.addView(headerLosses);

		TextView headerGoals = createTableHeaderCell(getResources().getString(R.string.table_header_goals_for), LAYOUT_WEIGHT_GOALS, Gravity.CENTER, 0);
		headerGoals.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.GOALS);
			}
		});
		row.addView(headerGoals);

		TextView headerGoalsAgainst = createTableHeaderCell(getResources().getString(R.string.table_header_goals_against), LAYOUT_WEIGHT_GOALS_AGAINST, Gravity.CENTER, 0);
		headerGoalsAgainst.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.GOALS_AGAINST);
			}
		});
		row.addView(headerGoalsAgainst);

		/*
		TextView headerWinLossRatio = createTableHeaderCell(getResources().getString(R.string.table_header_win_loss_ratio), LAYOUT_WEIGHT_WIN_LOSS_RATIO, Gravity.CENTER, 0);
		headerWinLossRatio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.WIN_LOSS_RATIO);
			}
		});
		row.addView(headerWinLossRatio);
		*/

		TextView headerRating = createTableHeaderCell(getResources().getString(R.string.table_header_rating), LAYOUT_WEIGHT_RATING, Gravity.CENTER, 0);
		headerRating.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sortByColumn(SortColumn.RATING);
			}
		});
		row.addView(headerRating);

		mLayout.addView(row);
	}

	/**
	 * Creates the player rows and adds them to the given table layout.
	 * @param layout The table layout.
	 */
	private void addTablePlayerRows(TableLayout layout) {
		int numPlayers = mPlayers.size();

		if (numPlayers > 0) {
			for (int i = 0; i < numPlayers; i++) {
				Player player = mPlayers.get(i);
				TableRow row = new TableRow(this);
				row.addView(createTableCell(player.getName(), LAYOUT_WEIGHT_PLAYER, Gravity.LEFT, 0));
				row.addView(createTableCell(String.valueOf(player.gamesPlayed()), LAYOUT_WEIGHT_GAMES_PLAYED, Gravity.CENTER, 0));
				row.addView(createTableCell(String.valueOf(player.getWins()), LAYOUT_WEIGHT_WINS, Gravity.CENTER, 0));
				row.addView(createTableCell(String.valueOf(player.getLosses()), LAYOUT_WEIGHT_LOSSES, Gravity.CENTER, 0));
				row.addView(createTableCell(String.valueOf(player.getGoalsFor()), LAYOUT_WEIGHT_GOALS, Gravity.CENTER, 0));
				row.addView(createTableCell(String.valueOf(player.getGoalsAgainst()), LAYOUT_WEIGHT_GOALS_AGAINST, Gravity.CENTER, 0));
				//row.addView(createTableCell(String.format("%2.01f", player.winLossRatio()), LAYOUT_WEIGHT_WIN_LOSS_RATIO, Gravity.CENTER, 0));
				row.addView(createTableCell(String.valueOf(player.getRating()), LAYOUT_WEIGHT_RATING, Gravity.CENTER, 0));
				mLayout.addView(row);
			}
		}
	}

	/**
	 * Creates a single table header cell.
	 * @param text Cell text.
	 * @param weight Cell weight.
	 * @param gravity Cell gravity (think align).
	 * @param padding Cell padding.
	 * @return The cell.
	 */
	private TextView createTableHeaderCell(String text, float weight, int gravity, int padding) {
		TextView cell = createTableCell(text, weight, gravity, padding);
		cell.setTypeface(null, Typeface.BOLD);
		return cell;
	}

	/**
	 * Creates a single table cell.
	 * @param text Cell text.
	 * @param weight Cell weight.
	 * @param gravity Cell gravity (think align).
	 * @param padding Cell padding.
	 * @return The cell.
	 */
	private TextView createTableCell(String text, float weight, int gravity, int padding) {
		TextView cell = new TextView(this);
		cell.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight));
		cell.setPadding(padding, padding, padding, padding);
		cell.setGravity(gravity);
		cell.setText(text);
		return cell;
	}

	/**
	 * Sorts the table by the given column.
	 * @param column Sort column type.
	 */
	private void sortByColumn(SortColumn column) {
		mComparator.sortColumn(column);
		Collections.sort(mPlayers, mComparator);
		mLayout.removeAllViews();
		addTableHeaderRow(mLayout);
		addTablePlayerRows(mLayout);
		mLayout.invalidate();
	}
}