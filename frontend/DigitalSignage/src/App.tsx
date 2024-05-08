import './App.css'

function App() {

  return (
    <>
      <div className='container'>

        <div className='grid-container'>
          <div className="grid-child">
            <h1>Preparing...</h1>
          </div>
          <div className="grid-child cyan">
            <h1>Delivery</h1>
          </div>
        </div>

        <div className='grid-container orders'>
          <div className="grid-child">
            <div className='order-container'>
              <h2 className="order-child">089</h2>
              <h2 className="order-child">090</h2>
              <h2 className="order-child">091</h2>
              <h2 className="order-child">092</h2>
              <h2 className="order-child">093</h2>
              <h2 className="order-child">094</h2>
              <h2 className="order-child">098</h2>
              <h2 className="order-child">099</h2>
              <h2 className="order-child">100</h2>
            </div>
          </div>
          <div className="grid-child">
            <div className='order-container cyan'>
              <h2 className="order-child">080</h2>
              <h2 className="order-child">084</h2>
              <h2 className="order-child">085</h2>
            </div>
          </div>
        </div>

      </div>
    </>
  )
}

export default App
