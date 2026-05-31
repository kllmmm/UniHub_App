from langchain_community.document_loaders import PyPDFLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.vectorstores import Chroma
from langchain_core.prompts import ChatPromptTemplate
from langchain_ollama import ChatOllama, OllamaEmbeddings
from langchain_classic.chains import create_retrieval_chain
from langchain_classic.chains.combine_documents import create_stuff_documents_chain
from langchain_huggingface import HuggingFaceEmbeddings

def setup_opa_model(pdf_path="odhgos_spoydwn.pdf"):
    print("Διάβασμα του PDF και τεμαχισμός κειμένου...")
    loader = PyPDFLoader(pdf_path)
    docs = loader.load()

    #text splitting
    text_splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=200)
    splits = text_splitter.split_documents(docs)


    print("Creating Vector Database (Local withOllama)...")

    print("Loading Multi-lingual Embeddings...")
    embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2")
    vectorstore = Chroma.from_documents(documents=splits, embedding=embeddings)
    
    retriever = vectorstore.as_retriever(search_kwargs={"k": 4})

    #LLM setup
    llm = ChatOllama(model="llama3", temperature=0)

    system_prompt = (
        "Είσαι ένας βοηθός για τους φοιτητές του τμήματος Πληροφορικής του ΟΠΑ. "
        "Χρησιμοποίησε ΜΟΝΟ τα παρακάτω αποσπάσματα από τον οδηγό σπουδών για να απαντήσεις στην ερώτηση. "
        "Αποσπάσματα: {context}"
    )

    prompt = ChatPromptTemplate.from_messages([
        ("system", system_prompt),
        ("human", "{input}"),
    ])

    #Chain setup
    question_answer_chain = create_stuff_documents_chain(llm, prompt)
    rag_chain = create_retrieval_chain(retriever, question_answer_chain)
    
    return rag_chain


"""-----Test----- """
#if __name__ == "__main__":
#    model_chain = setup_opa_model("odhgos_spoydwn.pdf")
#    
#    
#    while True:
#        user_input = input("\nΕρώτηση: ")
#        if user_input.lower() == 'έξοδος':
#            break
#            
#        response = model_chain.invoke({"input": user_input})
#        print(f"\nΑπάντηση: {response['answer']}")