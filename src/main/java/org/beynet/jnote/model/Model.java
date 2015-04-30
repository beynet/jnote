package org.beynet.jnote.model;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.util.Version;
import org.beynet.jnote.controler.AttachmentRef;
import org.beynet.jnote.controler.NoteBookRef;
import org.beynet.jnote.controler.NoteRef;
import org.beynet.jnote.controler.NoteSectionRef;
import org.beynet.jnote.exceptions.AttachmentAlreadyExistException;
import org.beynet.jnote.exceptions.AttachmentNotFoundException;
import org.beynet.jnote.model.events.model.NewNoteBookEvent;
import org.beynet.jnote.model.events.model.NoteBookRenamed;
import org.beynet.jnote.model.events.model.OnExitEvent;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Created by beynet on 06/04/2015.
 */
public class Model extends Observable implements FileVisitor<Path> {

    private static Model _instance = null;
    private final IndexWriter writer;
    private Analyzer analyzer ;




    public class MyAnalyser extends StopwordAnalyzerBase {

        public MyAnalyser() {
            super(StandardAnalyzer.STOP_WORDS_SET);
        }

        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            final Tokenizer source = new StandardTokenizer();

            TokenStream tokenStream = source;
            tokenStream = new StandardFilter( tokenStream);
            tokenStream = new LowerCaseFilter(tokenStream);
            tokenStream = new StopFilter(tokenStream, getStopwordSet());
            tokenStream = new ASCIIFoldingFilter(tokenStream);
            return new TokenStreamComponents(source, tokenStream);
        }
    }

    Model(Path rootDir) throws IOException {
        this.rootDir = rootDir;
        loadNoteBooks();
        //create lucene index
        Directory dir = FSDirectory.open(this.rootDir.resolve(".indexes"));
        analyzer = new MyAnalyser();

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        this.writer = new IndexWriter(dir, iwc);
    }

    public void deleteSection(NoteSectionRef ref) throws IOException {
        getNoteBookByUUID(ref.getNoteBookRef().getUUID()).deleteSection(ref,writer);
    }

    public static Model createInstance(Path rootDir) throws IOException {
        if (!Files.exists(rootDir)) {
            try {
                Files.createDirectories(rootDir);
            } catch (IOException e) {
                throw new IllegalArgumentException("unable to create target model directory",e);
            }
        }
        if (_instance==null) {
            _instance = new Model(rootDir);
            return _instance;
        }
        else {
            throw new IllegalArgumentException("instance already created");
        }
    }

    public static Model getInstance() {
        return _instance;
    }

    private void loadNoteBooks() {
        noteBooks=new HashMap<>();
        depth=-1;
        try {
            Files.walkFileTree(rootDir,this);
        } catch (IOException e) {
            logger.error("unable to load",e);
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (depth<1 ) {
            depth++;
            if (depth==1 && !dir.getFileName().toString().startsWith(".")) {
                currentNoteBook = new NoteBook(dir);
                noteBooks.put(currentNoteBook.getUUID(), currentNoteBook);
            }
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (depth==1) {
            if (file.getFileName().toString().endsWith(".zip")) {
                currentNoteBook.addExistingSection(file.getFileName());
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        depth--;
        return FileVisitResult.CONTINUE;
    }

    public void subscribeToNoteBook(String noteBookUUID,Observer observer) throws IllegalArgumentException {
        getNoteBookByUUID(noteBookUUID).addObserver(observer);
    }
    public void unSubscribeToNoteBook(String noteBookUUID,Observer observer) throws IllegalArgumentException {
        getNoteBookByUUID(noteBookUUID).deleteObserver(observer);
    }

    public void subscribeToNoteSection(String noteBookUUID, String sectionUUID,Observer observer) {
        getNoteBookByUUID(noteBookUUID).subscribeToNoteSection(sectionUUID, observer);
    }

    public void unSubscribeToNoteSection(String noteBookUUID, String sectionUUID,Observer observer) {
        getNoteBookByUUID(noteBookUUID).unSubscribeToNoteSection(sectionUUID, observer);
    }

    public void addObserver(Observer observer) {
        super.addObserver(observer);
        synchronized (noteBooks) {
            for (Map.Entry<String,NoteBook> entry : noteBooks.entrySet()) {
                observer.update(this, new NewNoteBookEvent(entry.getKey(),entry.getValue().getName()));
            }
        }
    }

    private NoteBook getNoteBookByUUID(String UUID) throws IllegalArgumentException {
        final NoteBook noteBook;
        synchronized (noteBooks) {
            noteBook = noteBooks.get(UUID);
        }
        if (noteBook==null) throw new IllegalArgumentException("invalid note book UUID");
        return noteBook;
    }

    public void createNewSection(String noteBookUUID) throws IOException {
        getNoteBookByUUID(noteBookUUID).createNewEmptySection();
    }
    public void saveNoteContent(String noteBookUUID, String sectionUUID, String noteUUID, String content) throws IOException{
        NoteBook noteBookByUUID = getNoteBookByUUID(noteBookUUID);
        noteBookByUUID.saveNoteContent(sectionUUID, noteUUID, content, writer);
    }

    public void changeSectionName(String noteBookUUID, String sectionUUID, String name) throws IOException{
        getNoteBookByUUID(noteBookUUID).changeSectionName(writer,sectionUUID, name);
    }
    public void changeNoteName(NoteSectionRef noteSectionRef, String noteUUID, String text) throws IOException{
        getNoteBookByUUID(noteSectionRef.getNoteBookRef().getUUID()).changeNoteName(noteSectionRef.getUUID(), noteUUID, text);
    }

    public void onExit() {
        setChanged();
        notifyObservers(new OnExitEvent());
    }
    public void addNote(NoteSectionRef noteSectionRef) throws IOException {
        getNoteBookByUUID(noteSectionRef.getNoteBookRef().getUUID()).addNote(noteSectionRef.getUUID());
    }


    public void addNoteBook(String name) throws IOException {
        boolean nameOk = true;
        synchronized (noteBooks) {
            for (Map.Entry<String, NoteBook> entry : noteBooks.entrySet()) {
                if (entry.getValue().getName().equals(name)) nameOk=false;
            }
            Path toCreate = rootDir.resolve(name);
            Files.createDirectory(toCreate);
            NoteBook noteBook = new NoteBook(toCreate);
            noteBooks.put(noteBook.getUUID(),noteBook);
            setChanged();
            notifyObservers(new NewNoteBookEvent(noteBook.getUUID(),noteBook.getName()));
        }
    }
    public void delNote(NoteRef noteRef) throws IOException {
        getNoteBookByUUID(noteRef.getNoteSectionRef().getNoteBookRef().getUUID()).delNote(noteRef);
    }

    public void delNoteBook(String noteBookUUID) throws IOException {
        NoteBook noteBookByUUID = getNoteBookByUUID(noteBookUUID);
        noteBookByUUID.delete(writer);
    }


    public String getNoteContent(NoteRef noteRef) throws IOException {
        return getNoteBookByUUID(noteRef.getNoteSectionRef().getNoteBookRef().getUUID()).getNoteContent(noteRef);
    }

    public void addAttachment(NoteRef noteRef, Path path) throws IOException, AttachmentAlreadyExistException {
        getNoteBookByUUID(noteRef.getNoteSectionRef().getNoteBookRef().getUUID()).addAttachment(noteRef, path);
    }


    public void subscribeToNote(NoteRef noteRef, Observer observer) {
        getNoteBookByUUID(noteRef.getNoteSectionRef().getNoteBookRef().getUUID()).subscribeToNote(noteRef, observer);
    }


    public void unSubscribeToNote(NoteRef noteRef, Observer observer) {
        getNoteBookByUUID(noteRef.getNoteSectionRef().getNoteBookRef().getUUID()).unSubscribeToNote(noteRef, observer);
    }


    public void deleteAttachment(AttachmentRef attachmentRef) throws IOException, AttachmentNotFoundException {
        getNoteBookByUUID(attachmentRef.getNoteRef().getNoteSectionRef().getNoteBookRef().getUUID()).deleteAttachment(attachmentRef);
    }
    public void saveAttachment(AttachmentRef attachmentRef, Path path) throws IOException, AttachmentNotFoundException {
        getNoteBookByUUID(attachmentRef.getNoteRef().getNoteSectionRef().getNoteBookRef().getUUID()).saveAttachment(attachmentRef, path);
    }

    public List<NoteRef> getMatchingNotes(String query) throws IOException {
        List<NoteRef> result = new ArrayList<>();
        try (IndexReader reader = createReader()) {
            IndexSearcher searcher = new IndexSearcher(reader);

            QueryParser parser = new QueryParser(LuceneConstants.NOTE_CONTENT,new MyAnalyser());
            final Query parsed ;
            try {
                parsed = parser.parse(query);
            } catch (ParseException e) {
                logger.error("error in query",e);
                throw new IOException("error in query",e);
            }
            /*BooleanQuery booleanQuery = new BooleanQuery();

            Query patternQuery = new WildcardQuery(new Term(LuceneConstants.NOTE_CONTENT, "*" + query + "*"));
            booleanQuery.add(patternQuery, BooleanClause.Occur.MUST);*/

            TopScoreDocCollector collector = TopScoreDocCollector.create(1000);
            searcher.search(parsed, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document document = searcher.doc(docId);
                Optional<NoteRef> noteRef = constructNoteRefFromDocument(document);
                noteRef.ifPresent((p)->result.add(p));
            }
            return result;
        }
    }

    private Optional<NoteRef> constructNoteRefFromDocument(Document document) {
        Optional<NoteRef> result = Optional.empty();
        String noteBookName = document.get(LuceneConstants.NOTE_BOOK_NAME);
        String sectionUUID = document.get(LuceneConstants.SECTION_UUID);
        String noteUUID = document.get(LuceneConstants.NOTE_UUID);
        String noteName = document.get(LuceneConstants.NOTE_NAME);

        NoteBook noteBook = null ;
        synchronized (noteBooks) {
            for (Map.Entry<String,NoteBook> entry :noteBooks.entrySet()) {
                NoteBook value = entry.getValue();
                if (value.getName().equals(noteBookName)) {
                    noteBook = value;
                    break;
                }
            }
        }
        if (noteBook!=null) {
            NoteSection section = null;
            try {
                section = noteBook.getSectionByUUID(sectionUUID);
            }catch(Exception e) {

            }
            if (section!=null) {
                NoteBookRef noteBookRef = new NoteBookRef(noteBook.getUUID(),noteBook.getName());
                NoteSectionRef sectionRef = new NoteSectionRef(noteBookRef,section.getUUID(),section.getName());
                result=Optional.of(new NoteRef(sectionRef,noteUUID,noteName));
            }
        }
        return result;
    }

    private IndexReader createReader() throws IOException {
        return DirectoryReader.open(writer, true);
    }

    /**
     * reindex all the notes
     * @throws IOException
     */
    public void reIndexAllNotes() throws IOException {
        writer.deleteAll();
        synchronized (noteBooks) {
            for (Map.Entry<String,NoteBook> entry : noteBooks.entrySet()) {
                entry.getValue().reIndexAllNotes(writer);
            }
        }
    }
    public void renameNoteBook(String currentUUID, String name) throws IOException {
        NoteBook noteBook = getNoteBookByUUID(currentUUID);
        noteBook.changeName(name);
        setChanged();
        notifyObservers(new NoteBookRenamed(noteBook.getUUID(), noteBook.getName()));
    }

    private Path rootDir ;
    private long depth ;
    Map<String,NoteBook> noteBooks ;
    private NoteBook currentNoteBook ;
    private final static Logger logger = Logger.getLogger(Model.class);


}
