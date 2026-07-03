package duanapp.main;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages an undo / redo history of {@link Mat} snapshots.
 *
 * <p>Internally maintains a list and a cursor:
 * <pre>
 *   index:  0  1  2  3  [cursor=3]
 *   undo → moves cursor left; redo → moves cursor right
 * </pre>
 *
 * <p>Inserting a new snapshot while not at the end discards all entries after
 * the cursor (standard "fork" behaviour).
 *
 * <p>Memory is capped at {@link #MAX_SIZE} frames — the oldest frame is dropped
 * when the cap is exceeded.
 */
public class ImageHistory {

    /** Maximum number of snapshots kept in memory. */
    private static final int MAX_SIZE = 100;

    private final List<Mat> snapshots = new ArrayList<>();
    private int cursor = -1;  // index of the "current" frame

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Pushes a clone of {@code mat} onto the history as the new current frame.
     * Any redo frames after the cursor are discarded.
     *
     * @param mat image to snapshot (will be cloned — caller keeps ownership)
     */
    public void push(Mat mat) {
        // Discard any redo frames
        if (cursor < snapshots.size() - 1) {
            snapshots.subList(cursor + 1, snapshots.size()).clear();
        }

        snapshots.add(mat.clone());
        cursor = snapshots.size() - 1;

        // Evict oldest if over budget
        if (snapshots.size() > MAX_SIZE) {
            snapshots.remove(0);
            cursor--;
        }
    }

    /**
     * Returns the previous snapshot (undo), moving the cursor one step back.
     *
     * @return the previous {@link Mat}, or {@link Optional#empty()} if already at start
     */
    public Optional<Mat> undo() {
        if (cursor <= 0) {
            return Optional.empty();
        }
        cursor--;
        return Optional.of(snapshots.get(cursor).clone());
    }

    /**
     * Returns the next snapshot (redo), moving the cursor one step forward.
     *
     * @return the next {@link Mat}, or {@link Optional#empty()} if already at end
     */
    public Optional<Mat> redo() {
        if (cursor >= snapshots.size() - 1) {
            return Optional.empty();
        }
        cursor++;
        return Optional.of(snapshots.get(cursor).clone());
    }

    /**
     * Returns a clone of the current snapshot without changing the cursor.
     *
     * @return current snapshot or {@link Optional#empty()} if history is empty
     */
    public Optional<Mat> current() {
        if (cursor < 0 || cursor >= snapshots.size()) {
            return Optional.empty();
        }
        return Optional.of(snapshots.get(cursor).clone());
    }

    /** @return {@code true} if undo is possible */
    public boolean canUndo() {
        return cursor > 0;
    }

    /** @return {@code true} if redo is possible */
    public boolean canRedo() {
        return cursor < snapshots.size() - 1;
    }

    /** @return number of snapshots currently held */
    public int size() {
        return snapshots.size();
    }

    /** @return {@code true} if no snapshots have been pushed yet */
    public boolean isEmpty() {
        return snapshots.isEmpty();
    }
}
