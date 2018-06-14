package aero.glass.renderer.mark;

import com.infomatiq.jsi.rtree.RTree;

import java.util.Set;

import gnu.trove.TIntProcedure;
import com.infomatiq.jsi.Rectangle;

/**
 * Created by premeczmatyas on 31/01/16.
 */
public class AeroOverlapDetector implements IAeroOverlapDetector {

    private Set<AeroMark> visible;
    private int id;
    private boolean toAdd;
    private final Rectangle r = new Rectangle(0, 0, 0, 0);
    private RTree rtree = null;

    public void init(Set<AeroMark> v) {
        visible = v;
        rtree = new RTree();
        rtree.init(null);
        id = 0;
    }

    public void check(final AeroMark m) {
        aero.glass.renderer.mark.Rectangle rv = m.getRealEstate();
        if (rv == null) {
            return;
        }

        r.set((float) rv.x.min, (float) rv.y.min,
                (float) rv.x.max, (float) rv.y.max);

        toAdd = true;
        if (toAdd) {
            rtree.intersects(r, new TIntProcedure() {
                @Override
                public boolean execute(int oId) {
                    toAdd = false;
                    return toAdd;
                }
            });
        }
        if (toAdd
                || (m.getPriority() <= AeroMarkInfo.PRIORITY_TRAFFIC
                && m.getSquaredDistanceToCamera() <= 160000000)) { // closer than 40km
            rtree.add(r, id++);
            visible.add(m);
        }
    }
}
