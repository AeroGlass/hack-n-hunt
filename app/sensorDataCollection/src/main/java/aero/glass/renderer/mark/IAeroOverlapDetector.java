package aero.glass.renderer.mark;

import java.util.Set;

/**
 * Created by premeczmatyas on 31/01/16.
 */
public interface IAeroOverlapDetector {
    /**
     * Initialize with a set where visible (non-overlapping, prioritized) labels
     * will be collected.
     *
     * @param visible visibility on init
     */
    void init(Set<AeroMark> visible);

    /**
     * Check a new mark for overlaps and add it to the visible set, if visible.
     *
     * @param mark
     *            the mark to check
     */
    void check(AeroMark mark);
}
