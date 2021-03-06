package au.gov.api.ingest

class Pipeline {

    var ingestObjs: MutableList<Pair<String, Any>> = mutableListOf()
    var manifest: Manifest? = Manifest()
    var repository: IngestorRepository? = IngestorRepository()

    constructor(meta: Manifest, assetIdx: Int, repo: IngestorRepository? = null) {
        manifest = meta
        manifest!!.assetIdx = assetIdx
        repository = repo
    }

    var pipeline: MutableList<PipeObject> = mutableListOf()

    fun addToPipeline(input: PipeObject) {
        var element = input
        element.repository = repository;
        element.manifest = manifest!!
        pipeline.add(element)
    }

    fun finaliseData() {
        pipeline.filter { it.type == PipeObject.PipeType.Data }
                .forEach {
                    it.execute()
                    ingestObjs.add(Pair((it as Data).id, (it as Data).getString()))
                }
    }

    fun finaliseEngine() {
        pipeline.filter { it.type == PipeObject.PipeType.Engine }
                .forEach {
                    (it as Engine).setData(*ingestObjs.toTypedArray())
                    ingestObjs.add(Pair(it.outputId, it.getOutput()))
                }
    }

    fun finaliseIngest() {
        pipeline.filter { it.type == PipeObject.PipeType.Ingestor }
                .forEach {
                    (it as Ingestor).setData(ingestObjs.last())
                    it.execute()
                    ingestObjs.add(Pair(it.toString(), it.output))
                }
    }

    fun finalise() {
        finaliseData()
        finaliseEngine()
    }

    fun ingest() {
        finaliseIngest()
    }

    fun execute() {
        finalise()
        ingest()
    }

    fun getLastPipelineOnject(): Any {
        return ingestObjs.last()
    }

}

abstract class PipeObject {
    enum class PipeType {
        Data, Engine, Ingestor
    }

    abstract fun execute()
    abstract val type: PipeType
    var manifest: Manifest = Manifest()
    var repository: IngestorRepository? = IngestorRepository()
}



