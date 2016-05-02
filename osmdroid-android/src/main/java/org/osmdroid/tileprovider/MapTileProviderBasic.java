package org.osmdroid.tileprovider;

import android.content.Context;

import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.INetworkAvailablityCheck;
import org.osmdroid.tileprovider.modules.MapTileAssetsProvider;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.MapTileSqlCacheProvider;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

/**
 * This top-level tile provider implements a basic tile request chain which includes a
 * {@link MapTileFilesystemProvider} (a file-system cache), a {@link MapTileFileArchiveProvider}
 * (archive provider), and a {@link MapTileDownloader} (downloads map tiles via tile source).
 * 
 * @author Marc Kurtz
 * 
 */
public class MapTileProviderBasic extends MapTileProviderArray implements IMapTileProviderCallback {

	final IFilesystemCache tileWriter;
	// private static final Logger logger = LoggerFactory.getLogger(MapTileProviderBasic.class);

	/**
	 * Creates a {@link MapTileProviderBasic}.
	 */
	public MapTileProviderBasic(final Context pContext) {
		this(pContext, TileSourceFactory.DEFAULT_TILE_SOURCE);
	}

	/**
	 * Creates a {@link MapTileProviderBasic}.
	 */
	public MapTileProviderBasic(final Context pContext, final ITileSource pTileSource) {
		this(new SimpleRegisterReceiver(pContext), new NetworkAvailabliltyCheck(pContext),
				pTileSource, pContext,null);
	}

	/**
	 * Creates a {@link MapTileProviderBasic}.
	 */
	public MapTileProviderBasic(final Context pContext, final ITileSource pTileSource, final IFilesystemCache cacheWriter) {
		this(new SimpleRegisterReceiver(pContext), new NetworkAvailabliltyCheck(pContext),
				pTileSource, pContext,cacheWriter);
	}

	/**
	 * Creates a {@link MapTileProviderBasic}.
	 */
	public MapTileProviderBasic(final IRegisterReceiver pRegisterReceiver,
			final INetworkAvailablityCheck aNetworkAvailablityCheck, final ITileSource pTileSource,
			final Context pContext, final IFilesystemCache cacheWriter) {
		super(pTileSource, pRegisterReceiver);

		if (cacheWriter!=null)
			tileWriter = cacheWriter;
		else
			tileWriter=new SqlTileWriter();

		final MapTileAssetsProvider assetsProvider = new MapTileAssetsProvider(
				pRegisterReceiver, pContext.getAssets(), pTileSource);
		mTileProviderList.add(assetsProvider);

		//final MapTileFilesystemProvider fileSystemProvider = new MapTileFilesystemProvider(
		//		pRegisterReceiver, pTileSource);
		//mTileProviderList.add(fileSystemProvider);

		final MapTileSqlCacheProvider cachedProvider = new MapTileSqlCacheProvider(pRegisterReceiver, pTileSource);
		mTileProviderList.add(cachedProvider);

		final MapTileFileArchiveProvider archiveProvider = new MapTileFileArchiveProvider(
				pRegisterReceiver, pTileSource);
		mTileProviderList.add(archiveProvider);

		final MapTileDownloader downloaderProvider = new MapTileDownloader(pTileSource, tileWriter,
				aNetworkAvailablityCheck);
		mTileProviderList.add(downloaderProvider);
	}

	@Override
	public IFilesystemCache getTileWriter() {
		return tileWriter;
	}
}
