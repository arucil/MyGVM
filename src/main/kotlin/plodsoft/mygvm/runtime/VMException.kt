package plodsoft.mygvm.runtime

class VMException(message: String, val pc: Int) : Exception(message)

