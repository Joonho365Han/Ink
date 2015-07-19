package eden.notebook.ink;

import android.content.SearchRecentSuggestionsProvider;

public class NoteSearchSuggestionProvider extends SearchRecentSuggestionsProvider {

    static final String AUTHORITY = "eden.notebook.ink.NoteSearchSuggestionProvider";
    static final int MODE = DATABASE_MODE_QUERIES;

    public NoteSearchSuggestionProvider() { setupSuggestions(AUTHORITY, MODE); }
}
