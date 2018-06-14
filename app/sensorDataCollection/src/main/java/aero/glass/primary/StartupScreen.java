package aero.glass.primary;

import org.glob3.mobile.generated.Color;
import org.glob3.mobile.generated.DownloaderImageBuilder;
import org.glob3.mobile.generated.GFont;
import org.glob3.mobile.generated.HUDQuadWidget;
import org.glob3.mobile.generated.HUDRelativePosition;
import org.glob3.mobile.generated.HUDRelativeSize;
import org.glob3.mobile.generated.HUDRenderer;
import org.glob3.mobile.generated.IG3MBuilder;
import org.glob3.mobile.generated.LabelImageBuilder;
import org.glob3.mobile.generated.URL;

/**
 * Utility class for startup screen.
 * Created by DrakkLord on 2016. 07. 15..
 */
public final class StartupScreen {

    /** Default text shown on the startup screen. */
    public static final String DEFAULT_PROGRESS_TEXT = "Initializing";

    private StartupScreen() {
        // empty in purpose
    }

    public static LabelImageBuilder createBusyRenderer(IG3MBuilder builder,
                                                        int displayWidth, int displayHeight) {
        // create the busy renderer
        HUDRelativePosition x, y;
        HUDRelativeSize width, height;
        LabelImageBuilder progLabel;

        HUDRenderer busyRenderer = new HUDRenderer();

        // get the real width and from that calculate the real height
        float realWidth = displayWidth * 0.75f;
        float realHeight = realWidth / 2.0f; // the image's aspect ratio is 2:1

        x = new HUDRelativePosition(0.5f, HUDRelativePosition.Anchor.VIEWPORT_WIDTH,
                                    HUDRelativePosition.Align.CENTER);

        y = new HUDRelativePosition(0.5f, HUDRelativePosition.Anchor.VIEWPORT_HEIGHT,
                                    HUDRelativePosition.Align.MIDDLE);

        width = new HUDRelativeSize(0.75f, HUDRelativeSize.Reference.VIEWPORT_WIDTH);
        height = new HUDRelativeSize(realHeight / displayHeight,
                                     HUDRelativeSize.Reference.VIEWPORT_HEIGHT);

        DownloaderImageBuilder imageBuilder =
                new DownloaderImageBuilder(new URL("file:///aero_logo.png"));

        HUDQuadWidget logo = new HUDQuadWidget(imageBuilder, x, y, width, height);

        // add the application text
        LabelImageBuilder appLabel = new LabelImageBuilder("Aero Glass", // text
                                                           GFont.sansSerif(38, true), // font
                                                           28, // margin
                                                           Color.white(), // color
                                                           Color.transparent(), // shadow color
                                                           0, // shadow blur
                                                           0, // shadow offset x
                                                           0, // shadow offset y
                                                           Color.transparent(), // bg color
                                                           0, // corner radius
                                                           false // mutable
        );

        y = new HUDRelativePosition(0.35f, HUDRelativePosition.Anchor.VIEWPORT_HEIGHT,
                                    HUDRelativePosition.Align.BELOW);

        width = new HUDRelativeSize(0.4f, HUDRelativeSize.Reference.VIEWPORT_WIDTH);
        height = new HUDRelativeSize(0.2f, HUDRelativeSize.Reference.VIEWPORT_HEIGHT);

        HUDQuadWidget appText = new HUDQuadWidget(appLabel, x, y, width, height);

        // add the loading progress text
        progLabel = new LabelImageBuilder(DEFAULT_PROGRESS_TEXT, // text
                                          GFont.sansSerif(28, true), // font
                                          28, // margin
                                          Color.white(), // color
                                          Color.transparent(), // shadow color
                                          0, // shadow blur
                                          0, // shadow offset x
                                          0, // shadow offset y
                                          Color.transparent(), // bg color
                                          0, // corner radius
                                          true // mutable
        );

        y = new HUDRelativePosition(0.26f, HUDRelativePosition.Anchor.VIEWPORT_HEIGHT,
                                    HUDRelativePosition.Align.BELOW);

        width = new HUDRelativeSize(1.0f, HUDRelativeSize.Reference.BITMAP_MAX_AXIS);
        height = new HUDRelativeSize(0.15f, HUDRelativeSize.Reference.VIEWPORT_HEIGHT);

        HUDQuadWidget progText = new HUDQuadWidget(progLabel, x, y, width, height);

        // the order matters here!
        busyRenderer.addWidget(appText);
        busyRenderer.addWidget(progText);
        busyRenderer.addWidget(logo);

        builder.setBusyRenderer(busyRenderer);

        return progLabel;
    }
}
